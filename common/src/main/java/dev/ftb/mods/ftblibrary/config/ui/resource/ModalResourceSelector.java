package dev.ftb.mods.ftblibrary.config.ui.resource;

import com.google.common.base.Stopwatch;
import com.mojang.blaze3d.platform.InputConstants;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ResourceConfigValue;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.config.ui.EditMultilineStringConfigOverlay;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.SNBTSyntaxException;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.LowerButtonPanel;
import dev.ftb.mods.ftblibrary.ui.misc.SimpleToast;
import dev.ftb.mods.ftblibrary.util.SearchTerms;
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

public abstract class ModalResourceSelector<T> extends ModalPanel {
    private static final int ITEM_COLS = 9;
    private static final int ITEM_ROWS = 5;

    private static final ExecutorService SEARCH_EXECUTOR = Executors.newSingleThreadExecutor(task -> {
        var thread = new Thread(task, "FTBLibrary-ItemSearch");
        thread.setDaemon(true);
        return thread;
    });

    private final CustomTopPanel topPanel;
    private final Panel mainPanel;
    private final Panel bottomPanel;

    private final ResourceConfigValue<T> config;
    private final ConfigCallback callback;
    private final PanelScrollBar scrollBar;
    public long update = Long.MAX_VALUE;
    private SelectableResource<T> selectedStack;
    private int refreshTimer = 0;
    private int nRows = ITEM_ROWS;
    private int nCols = ITEM_COLS;

    public ModalResourceSelector(Panel parent, ResourceConfigValue<T> config, ConfigCallback callback) {
        super(parent);

        this.config = config;
        this.callback = callback;

        topPanel = new CustomTopPanel();
        mainPanel = new StacksPanel();
        bottomPanel = new LowerButtonPanel(this, this::doAccept, this::doCancel);
        scrollBar = new PanelScrollBar(this, mainPanel);

        scrollBar.setCanAlwaysScroll(true);
        scrollBar.setScrollStep(19);

        topPanel.setSelected(config.getResource());
    }

    public ModalResourceSelector<T> withGridSize(int rows, int cols) {
        Validate.isTrue(rows >= 1 && cols >= 1);
        nRows = rows;
        nCols = cols;
        return this;
    }

    @Override
    public void addWidgets() {
        add(topPanel);
        add(mainPanel);
        add(scrollBar);
        add(bottomPanel);
    }

    @Override
    public void alignWidgets() {
        setWidth(19 + 18 * nCols);
        setHeight(108 + 18 * nRows);

        topPanel.setSize(width, 78);
        topPanel.alignWidgets();

        mainPanel.setPosAndSize(2, topPanel.getHeight() + 2, 18 * nCols, 18 * nRows);
        scrollBar.setPosAndSize(mainPanel.getPosX() + mainPanel.getWidth() + 2, mainPanel.getPosY(), 12, mainPanel.getHeight());

        int yPos = mainPanel.posY + mainPanel.height + 2;
        bottomPanel.setPosAndSize(0, yPos, width, height - yPos);
        bottomPanel.alignWidgets();
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        theme.drawGui(graphics, x, y, w, h, WidgetType.NORMAL);
    }

    @Override
    public void tick() {
        super.tick();

        if (refreshTimer > 0 && --refreshTimer == 0) {
            mainPanel.refreshWidgets();
        }

        if (Util.getMillis() >= update) {
            update = Long.MAX_VALUE;
            CompletableFuture.supplyAsync(() -> this.makeResourceWidgets(topPanel.getSearchText()), SEARCH_EXECUTOR)
                    .thenAcceptAsync(this::updateItemWidgets, Minecraft.getInstance());
        }
    }

    @Override
    public boolean keyPressed(Key key) {
        if (key.is(InputConstants.KEY_RETURN)) {
            doAccept();
            return true;
        }
        return super.keyPressed(key);
    }

    protected void doCancel() {
        callback.save(false);
        getGui().closeModalPanel(this);
    }

    protected void doAccept() {
        boolean changed = config.setResource(selectedStack);
        callback.save(changed);
        getGui().closeModalPanel(this);
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

        SearchTerms searchTerms = SearchTerms.parse(search);

        ResourceButton emptyButton = makeResourceButton(mainPanel, null);
        if (config.allowEmptyResource() && emptyButton.shouldAdd(searchTerms)) {
            emptyButton.setPos(1, 1);
            widgets.add(emptyButton);
        }

        for (SelectableResource<T> selectable : items) {
            if (!selectable.isEmpty()) {
                ResourceButton button = makeResourceButton(mainPanel, selectable);
                if (config.allowResource(selectable.resource()) && button.shouldAdd(searchTerms)) {
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
            super(ModalResourceSelector.this);
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

    protected abstract class ResourceButton extends Button {
        protected final SelectableResource<T> selectable;

        protected ResourceButton(Panel panel, SelectableResource<T> is) {
            super(panel, Component.empty(), Icons.BARRIER);

            setSize(18, 18);
            selectable = is;
            title = null;
            icon = selectable.getIcon();
        }

        public T getResource() {
            return selectable.resource();
        }

        public abstract boolean shouldAdd(SearchTerms search);

        @Override
        public Component getTitle() {
            if (title == null) {
                title = selectable.getName();
            }

            return title;
        }

        @Override
        public void addMouseOverText(TooltipList list) {
            // nothing
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            theme.drawSlot(graphics, x, y, w, h, WidgetType.NORMAL);
            if (isMouseOver) {
                Color4I.WHITE.withAlpha(30).draw(graphics, x + 1, y + 1, w - 2, h - 2);
            }
        }

        @Override
        public void drawIcon(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            super.drawIcon(graphics, theme, x + 1, y + 1, w - 2, h - 2);
        }

        @Override
        public void onClicked(MouseButton button) {
            playClickSound();
            topPanel.setSelected(selectable); //.copyWithCount(Math.max(1, countBox.getCount())));
        }

        @Override
        public boolean mouseDoubleClicked(MouseButton button) {
            if (isMouseOver()) {
                topPanel.setSelected(selectable.copyWithCount(Math.max(1, topPanel.countBox.getCount())));
                doAccept();
                return true;
            }
            return false;
        }
    }

    private class CustomTopPanel extends Panel {
        private final TextField selectedLabel;
        private final TextBox searchBox;
        private final CountTextBox countBox;
        private final Button upBtn, downBtn;
        private final SearchModeButton searchModeButton;
        private final NBTButton nbtButton;

        public CustomTopPanel() {
            super(ModalResourceSelector.this);

            searchModeButton = new SearchModeButton(this);

            nbtButton = new NBTButton(this);

            selectedLabel = new TextField(this).addFlags(Theme.SHADOW);

            searchBox = new TextBox(this) {
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
        }

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
            selectedLabel.setPosAndSize(5, 5, getGui().width - 10, getGui().getTheme().getFontHeight());
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

        @Override
        public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            super.draw(graphics, theme, x, y, w, h);

            if (!selectedStack.isEmpty()) {
                selectedStack.getIcon().drawStatic(graphics, getX() + 6, getY() + 17, 30, 30);
                GuiHelper.drawRectWithShade(graphics, getX() + 5, getY() + 16, 32, 32, Color4I.DARK_GRAY, -16);
                if (countBox.shouldDraw()) {
                    theme.drawString(graphics, "x", getX() + 38, getY() + 28, Theme.SHADOW);
                }
            }
        }

        String getSearchText() {
            return searchBox.getText().toLowerCase();
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

        private class AdjusterButton extends SimpleTextButton {
            private final boolean up;

            public AdjusterButton(boolean up) {
                super(CustomTopPanel.this, Component.literal(up ? "+" : "-"), Icon.empty());
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


        private class CountTextBox extends TextBox {
            private static final Pattern INTEGER = Pattern.compile("^\\d+$");

            public CountTextBox() {
                super(CustomTopPanel.this);
            }

            @Override
            public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
                if (!isMouseOver) return false;

                var directionlessDelta = xDelta != 0 ? xDelta : yDelta;
                if (isShiftKeyDown()) {
                    int adj = directionlessDelta > 0 ? getCount() : -getCount() / 2;
                    adjust(adj);
                } else {
                    adjust((int) Math.signum(directionlessDelta));
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


        private class NBTButton extends Button {
            public NBTButton(Panel panel) {
                super(panel, Component.translatable("ftblibrary.select_item.nbt"), ItemIcon.getItemIcon(Items.NAME_TAG));
            }

            @Override
            public void onClicked(MouseButton button) {
                playClickSound();

                CompoundTag toEdit = Objects.requireNonNullElse(selectedStack.getComponentsTag(), new CompoundTag());
                if (button.isLeft()) {
                    StringConfig config = new StringConfig();
                    SNBTCompoundTag snbt = SNBTCompoundTag.of(toEdit);
                    snbt.singleLine();
                    config.setCurrentValue(String.join(",", SNBT.writeLines(snbt)));
                    getGui().pushModalPanel(makeMultilineEditPanel(config));
                } /*else if (button.isRight()) {
                    CompoundTag info = Util.make(new CompoundTag(), tag -> tag.putString("type", "item"));
                    new NBTEditorScreen(info, toEdit, (accepted, tag) -> {
                        if (accepted) {
                            selectedStack.applyComponentsTag(tag.copy());
                            ModalResourceSelector.this.openGuiLater();
                        }
                    }).openGui();
                }*/
            }

            @NotNull
            private EditMultilineStringConfigOverlay makeMultilineEditPanel(StringConfig config) {
                var panel = new EditMultilineStringConfigOverlay(ModalResourceSelector.this, config, accepted -> {
                    if (accepted) {
                        try {
                            selectedStack.applyComponentsTag(SNBT.readLines(List.of(config.getValue())));
                        } catch (SNBTSyntaxException e) {
                            SimpleToast.error(Component.translatable("ftblibrary.gui.error"), Component.literal(e.getMessage()));
                        }
                    }
                });
                panel.setExtraZlevel(300);  // ensure it renders over any rendered items
                int w = getWindow().getGuiScaledWidth() - 10 - getX();
                panel.setPosAndSize(getPosX(), getPosY() + getHeight(), w, 50);
                return panel;
            }

            @Override
            public boolean shouldDraw() {
                return !selectedStack.isEmpty();
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
                        list.add(mode.getDisplayName().copy().withStyle(ChatFormatting.GRAY)
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
    }
}
