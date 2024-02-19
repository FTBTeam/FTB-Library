package dev.ftb.mods.ftblibrary.config.ui;

import com.google.common.base.Stopwatch;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ResourceConfigValue;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.nbtedit.NBTEditorScreen;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.SNBTSyntaxException;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.AbstractThreePanelScreen;
import dev.ftb.mods.ftblibrary.ui.misc.SimpleToast;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public abstract class ResourceSelectorScreen<T> extends AbstractThreePanelScreen<ResourceSelectorScreen<T>.StacksPanel> {
    private static final int ITEM_COLS = 9;
    private static final int ITEM_ROWS = 5;

    private static final ExecutorService SEARCH_EXECUTOR = Executors.newSingleThreadExecutor(task -> {
        var thread = new Thread(task, "FTBLibrary-ItemSearch");
        thread.setDaemon(true);
        return thread;
    });

    private final ResourceConfigValue<T> config;
    private final ConfigCallback callback;
    private SelectableResource<T> selectedStack;
    private int refreshTimer = 0;

    private final TextField selectedLabel;
    private final TextBox searchBox;
    private final CountTextBox countBox;
    private final Button upBtn, downBtn;
    private final SearchModeButton searchModeButton;
    private final NBTButton nbtButton;

    private int nRows = ITEM_ROWS;
    private int nCols = ITEM_COLS;

    public long update = Long.MAX_VALUE;

    public ResourceSelectorScreen(ResourceConfigValue<T> config, ConfigCallback callback) {
        super();

        this.config = config;
        this.callback = callback;

        searchModeButton = new SearchModeButton(topPanel);

        nbtButton = new NBTButton(topPanel);

        selectedLabel = new TextField(topPanel);

        searchBox = new TextBox(topPanel) {
            @Override
            public void onTextChanged() {
                refreshTimer = 5;
            }
        };
        searchBox.ghostText = I18n.get("gui.search_box");
        searchBox.setFocused(true);

        countBox = new CountTextBox();
        upBtn = new AdjusterButton(true);
        downBtn = new AdjusterButton(false);

        scrollBar.setCanAlwaysScroll(true);
        scrollBar.setScrollStep(19);

        setSelected(config.getResource());
    }

    public ResourceSelectorScreen<T> withGridSize(int rows, int cols) {
        Validate.isTrue(rows >= 1 && cols >= 1);
        nRows = rows;
        nCols = cols;
        return this;
    }

    @Override
    public boolean onInit() {
        setWidth(19 + 18 * nCols);
        setHeight(108 + 18 * nRows);
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (refreshTimer > 0 && --refreshTimer == 0) {
            mainPanel.refreshWidgets();
        }
    }

    @Override
    protected int getTopPanelHeight() {
        return 78;
    }

    @Override
    protected StacksPanel createMainPanel() {
        return new StacksPanel();
    }

    @Override
    protected Panel createTopPanel() {
        return new CustomTopPanel();
    }

    @Override
    protected Pair<Integer, Integer> mainPanelInset() {
        return Pair.of(2, 2);
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        super.drawBackground(graphics, theme, x, y, w, h);

        if (Util.getMillis() >= update) {
            update = Long.MAX_VALUE;
            CompletableFuture.supplyAsync(() -> this.makeResourceWidgets(searchBox.getText().toLowerCase()), SEARCH_EXECUTOR)
                    .thenAcceptAsync(this::updateItemWidgets, Minecraft.getInstance());
        }
    }

    @Override
    public void drawForeground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        if (!selectedStack.isEmpty()) {
            selectedStack.getIcon().drawStatic(graphics, getX() + 6, getY() + 17, 30, 30);
            GuiHelper.drawRectWithShade(graphics, getX() + 5, getY() + 16, 32, 32, Color4I.DARK_GRAY, -16);
            if (countBox.shouldDraw()) {
                theme.drawString(graphics, "x", getX() + 38, getY() + 28, Theme.SHADOW);
            }
        }
    }

    @Override
    public boolean keyPressed(Key key) {
        if ((key.is(InputConstants.KEY_RETURN) || key.is(InputConstants.KEY_NUMPADENTER)) && key.modifiers.shift()) {
            doAccept();
            return true;
        } else {
            return super.keyPressed(key);
        }
    }

    protected void setSelected(SelectableResource<T> stack) {
        long count = selectedStack == null || selectedStack.isEmpty() ? Math.max(stack.getCount(), countBox.getCount()) : selectedStack.getCount();
        selectedStack = stack.copyWithCount(count);

        Component name = selectedStack.isEmpty() ?
                Component.translatable("ftblibrary.gui.no_selection").withStyle(ChatFormatting.ITALIC) :
                selectedStack.getName();
        selectedLabel.setText(name);
        countBox.setText(Long.toString(config.fixedResourceSize().orElse(selectedStack.getCount())), false);
    }

    @Override
    protected void doCancel() {
        callback.save(false);
    }

    @Override
    protected void doAccept() {
        boolean changed = config.setResource(selectedStack);
        callback.save(changed);
    }

    protected int defaultQuantity() {
        return 1;
    }

    protected abstract ResourceButton makeResourceButton(Panel panel, @Nullable SelectableResource<T> resource);

    protected abstract SearchModeIndex<ResourceSearchMode<T>> getSearchModeIndex();

    private Optional<ResourceSearchMode<T>> getActiveSearchMode() {
        return getSearchModeIndex().getCurrentSearchMode();
    }

    public List<Widget> makeResourceWidgets(String search) {
        var timer = Stopwatch.createStarted();

        if (getActiveSearchMode().isEmpty()) {
            return Collections.emptyList();
        }

        var items = getActiveSearchMode().get().getAllResources();
        List<Widget> widgets = new ArrayList<>(search.isEmpty() ? items.size() + 1 : 64);

        ResourceButton emptyButton = makeResourceButton(mainPanel, null);
        if (config.allowEmptyResource() && emptyButton.shouldAdd(search)) {
            emptyButton.setPos(1, 1);
            widgets.add(emptyButton);
        }

        for (SelectableResource<T> resource : items) {
            if (!resource.isEmpty()) {
                ResourceButton button = makeResourceButton(mainPanel, resource);
                if (button.shouldAdd(search)) {
                    widgets.add(button);
                    var idx = widgets.size() - 1;
                    button.setPos(1 + (idx % nCols) * 18, 1 + (idx / nCols) * 18);
                }
            }
        }

        FTBLibrary.LOGGER.info("Done updating item list in {}μs!", timer.stop().elapsed(TimeUnit.MICROSECONDS));
        return widgets;
    }

    private void updateItemWidgets(List<Widget> items) {
        mainPanel.getWidgets().clear();
        mainPanel.addAll(items);
        scrollBar.setValue(0);
    }

    public class StacksPanel extends Panel {
        public StacksPanel() {
            super(ResourceSelectorScreen.this);
        }

        @Override
        public void addWidgets() {
            update = Util.getMillis() + 50L;
        }

        @Override
        public void alignWidgets() {
        }

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            super.draw(graphics, theme, x, y, w, h);

            GuiHelper.drawHollowRect(graphics, x - 1, y - 1, w + 2, h + 2, Color4I.rgb(0x101010), false);
        }
    }

    private class SearchModeButton extends Button {
        public SearchModeButton(Panel panel) {
            super(panel);

            setSize(20, 20);
        }

        @Override
        public void drawIcon(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            getActiveSearchMode().ifPresent(mode -> mode.getIcon().draw(graphics, x, y, w, h));
        }

        @Override
        public Component getTitle() {
            return Component.translatable("ftblibrary.select_item.list_mode");
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            super.addMouseOverText(list);
            getActiveSearchMode().ifPresent(mode ->
                    list.add(mode.getDisplayName().withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(" [" + mainPanel.getWidgets().size() + "]").withStyle(ChatFormatting.DARK_GRAY)))
            );
        }

        @Override
        public void onClicked(MouseButton button) {
            playClickSound();
            getSearchModeIndex().nextMode();
            mainPanel.refreshWidgets();
        }
    }

    private class NBTButton extends Button {
        public NBTButton(Panel panel) {
            super(panel, Component.translatable("ftblibrary.select_item.nbt"), ItemIcon.getItemIcon(Items.NAME_TAG));
        }

        @Override
        public void onClicked(MouseButton button) {
            playClickSound();

            CompoundTag toEdit = Objects.requireNonNullElse(selectedStack.getTag(), new CompoundTag());
            if (button.isLeft()) {
                StringConfig config = new StringConfig();
                SNBTCompoundTag snbt = SNBTCompoundTag.of(toEdit);
                snbt.singleLine();
                config.setCurrentValue(String.join(",", SNBT.writeLines(snbt)));
                getGui().pushModalPanel(makeMultilineEditPanel(config));
            } else if (button.isRight()) {
                CompoundTag info = Util.make(new CompoundTag(), tag -> tag.putString("type", "item"));
                new NBTEditorScreen(info, toEdit, (accepted, tag) -> {
                    if (accepted) {
                        selectedStack.setTag(tag.copy());
                        ResourceSelectorScreen.this.openGuiLater();
                    }
                }).openGui();
            }
        }

        @NotNull
        private EditMultilineStringConfigOverlay makeMultilineEditPanel(StringConfig config) {
            var panel = new EditMultilineStringConfigOverlay(ResourceSelectorScreen.this, config, accepted -> {
                if (accepted) {
                    try {
                        selectedStack.setTag(SNBT.readLines(List.of(config.getValue())));
                    } catch (SNBTSyntaxException e) {
                        SimpleToast.error(Component.translatable("ftblibrary.gui.error"), Component.literal(e.getMessage()));
                    }
                }
            });
            int w = getScreen().getGuiScaledWidth() - 10 - getX();
            panel.setPosAndSize(getPosX(), getPosY() + getHeight(), w, 50);
            return panel;
        }

        @Override
        public boolean shouldDraw() {
            return !selectedStack.isEmpty();
        }
    }

    private class CountTextBox extends TextBox {
        private static final Pattern INTEGER = Pattern.compile("^\\d+$");

        public CountTextBox() {
            super(topPanel);
        }

        @Override
        public boolean mouseScrolled(double scroll) {
            if (!isMouseOver) return false;
            if (isShiftKeyDown()) {
                int adj = scroll > 0 ? getCount() : -getCount() / 2;
                adjust(adj);
            } else {
                adjust((int) Math.signum(scroll));
            }
            return true;
        }

        @Override
        public void onTextChanged() {
            if (!selectedStack.isEmpty()) {
                selectedStack.setCount(Math.max(1, getCount()));
            }
        }

        @Override
        public boolean isValid(String txt) {
            return INTEGER.matcher(txt).matches();
        }

        private int getCount() {
            try {
                return Integer.parseInt(getText());
            } catch (NumberFormatException ignored) {
                return 1;
            }
        }

        void adjust(int offset) {
            int count = Math.max(1, getCount() + offset);
            setText(Integer.toString(count));
        }

        @Override
        public boolean shouldDraw() {
            return config.fixedResourceSize().isEmpty() && !selectedStack.isEmpty();
        }
    }

    protected abstract class ResourceButton extends Button {
        protected final SelectableResource<T> resource;

        protected ResourceButton(Panel panel, SelectableResource<T> is) {
            super(panel, Component.empty(), Icons.BARRIER);

            setSize(18, 18);
            resource = is;
            title = null;
            icon = resource.getIcon();
        }

        public T getStack() {
            return resource.stack();
        }

        public abstract boolean shouldAdd(String search);

        @Override
        public Component getTitle() {
            if (title == null) {
                title = resource.getName();
            }

            return title;
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            // nothing
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            theme.drawSlot(graphics, x, y, w, h, getWidgetType());
        }

        @Override
        public void onClicked(MouseButton button) {
            playClickSound();
            setSelected(resource); //.copyWithCount(Math.max(1, countBox.getCount())));
        }

        @Override
        public boolean mouseDoubleClicked(MouseButton button) {
            if (isMouseOver()) {
                setSelected(resource.copyWithCount(Math.max(1, countBox.getCount())));
                doAccept();
                return true;
            }
            return false;
        }
    }

    private class AdjusterButton extends SimpleTextButton {
        private final boolean up;

        public AdjusterButton(boolean up) {
            super(topPanel, Component.literal(up ? "+" : "-"), Icon.empty());
            this.up = up;
        }

        @Override
        public void onClicked(MouseButton button) {
            int amt = isShiftKeyDown() ? (up ? countBox.getCount() : countBox.getCount() / 2) : 1;
            if (amt != 0) {
                countBox.adjust(up ? amt : -amt);
            }
        }

        @Override
        public boolean shouldDraw() {
            return config.fixedResourceSize().isEmpty() && !selectedStack.isEmpty();
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            // none
        }
    }

    private class CustomTopPanel extends TopPanel {
        @Override
        public void addWidgets() {
            add(selectedLabel);
            add(searchModeButton);
            add(searchBox);
            if (config.canHaveNBT()) {
                add(nbtButton);
            }
            add(countBox);
            add(upBtn);
            add(downBtn);
        }

        @Override
        public void alignWidgets() {
            selectedLabel.setPosAndSize(5, 5, getGui().width - 10, getTheme().getFontHeight());
            selectedLabel.setMaxWidth(selectedLabel.width).setTrim().showTooltipForLongText();

            searchModeButton.setPos(5, 54);
            searchBox.setPosAndSize(searchModeButton.posX + 22, 56, 100, 16);

            countBox.setPosAndSize(46, 25, 45, 16);
            upBtn.setPosAndSize(countBox.posX + countBox.width + 1, 21, 12, 12);
            downBtn.setPosAndSize(countBox.posX + countBox.width + 1, 33, 12, 12);

            if (config.canHaveNBT()) {
                int x = config.fixedResourceSize().isEmpty() ? upBtn.getPosX() + 16 : 44;
                nbtButton.setPosAndSize(x, 23, 20, 20);
            }
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            super.drawBackground(graphics, theme, x, y, w, h);

            if (!selectedStack.isEmpty()) {
                theme.drawSlot(graphics, getX() + 5, getY() + 16, 32, 32, WidgetType.NORMAL);
            }
        }
    }

}
