package dev.ftb.mods.ftblibrary.ui;

import dev.architectury.platform.Platform;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.KeyModifiers;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ScreenWrapper extends Screen implements IScreenWrapper {
    private final BaseScreen wrappedGui;
    private final TooltipList tooltipList;

    public ScreenWrapper(BaseScreen wrappedGui) {
        super(wrappedGui.getTitle());

        this.wrappedGui = wrappedGui;
        tooltipList = new TooltipList();
    }

    @Override
    public void init() {
        super.init();
//		wrappedGui.itemRenderer = itemRenderer;
        wrappedGui.initGui();
    }

    @Override
    public boolean isPauseScreen() {
        return wrappedGui.doesGuiPauseGame();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean flag) {
        wrappedGui.updateMouseOver((int) event.x(), (int) event.y());

        if (event.button() == MouseButton.BACK.id) {
            wrappedGui.onBack();
            return true;
        } else {
            return wrappedGui.mousePressed(MouseButton.get(event.button())) || super.mouseClicked(event, flag);
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        wrappedGui.updateMouseOver((int) event.x(), (int) event.y());
        wrappedGui.mouseReleased(MouseButton.get(event.button()));
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dirX, double dirY) {
        return wrappedGui.mouseScrolled(dirY) || super.mouseScrolled(x, y, dirX, dirY);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double dragX, double dragY) {
        return wrappedGui.mouseDragged(mouseButtonEvent.button(), dragX, dragY) || super.mouseDragged(mouseButtonEvent, dragX, dragY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        var key = new Key(event.key(), event.scancode(), event.modifiers(), event);

        if (wrappedGui.keyPressed(key)) {
            return true;
        } else {
            if (key.backspace()) {
                wrappedGui.onBack();
                return true;
            } else if (wrappedGui.onClosedByKey(key)) {
                if (shouldCloseOnEsc()) {
                    wrappedGui.closeGui(true);
                }
                return true;
            } else if (Platform.isModLoaded("jei")) {
                wrappedGui.getIngredientUnderMouse().ifPresent(underMouse -> handleIngredientKey(key, underMouse.ingredient()));
            }

            return super.keyPressed(event);
        }
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        var key = new Key(event.key(), event.scancode(), event.modifiers(), event);
        wrappedGui.keyReleased(key);
        return super.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (wrappedGui.charTyped(Character.forDigit(event.codepoint(), Character.MAX_RADIX), new KeyModifiers(event.modifiers()))) {
            return true;
        }

        return super.charTyped(event);
    }

    private void handleIngredientKey(Key key, Object object) {
        //FIXME: FTBLibJEIIntegration.handleIngredientKey(key, object);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        wrappedGui.updateGui(mouseX, mouseY, partialTicks);
        var x = wrappedGui.getX();
        var y = wrappedGui.getY();
        var w = wrappedGui.width;
        var h = wrappedGui.height;
        var theme = wrappedGui.getTheme();

        wrappedGui.draw(graphics, theme, x, y, w, h);
        wrappedGui.drawForeground(graphics, theme, x, y, w, h);
        wrappedGui.addMouseOverText(tooltipList);

        graphics.pose().pushMatrix();
        if (!tooltipList.shouldRender()) {
            wrappedGui.getIngredientUnderMouse().ifPresent(underMouse -> {
                if (underMouse.tooltip()) {
                    var ingredient = underMouse.ingredient();
                    if (ingredient instanceof ItemStack stack && !stack.isEmpty()) {
                        graphics.setTooltipForNextFrame(theme.getFont(), stack, mouseX, mouseY);
                    }
                }
            });
        } else {
            graphics.setTooltipForNextFrame(theme.getFont(), tooltipList.getLines(), Optional.empty(), mouseX, Math.max(mouseY, 18));
        }
        graphics.pose().popMatrix();

        tooltipList.reset();
    }

    @Override
    public void renderBackground(GuiGraphics matrixStack, int x, int y, float partialTicks) {
        if (wrappedGui.drawDefaultBackground(matrixStack)) {
            super.renderBackground(matrixStack, x, y, partialTicks);
        }
    }

    @Override
    protected void renderBlurredBackground(GuiGraphics guiGraphics) {
        if (wrappedGui.shouldRenderBlur()) {
            super.renderBlurredBackground(guiGraphics);
        }
    }

    @Override
    public void tick() {
        super.tick();
        wrappedGui.tick();
    }

    @Override
    public BaseScreen getGui() {
        return wrappedGui;
    }

    @Override
    public void removed() {
        wrappedGui.onClosed();
        super.removed();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return getGui().shouldCloseOnEsc();
    }
}
