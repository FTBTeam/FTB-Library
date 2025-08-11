package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.KeyModifiers;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class MenuScreenWrapper<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements IScreenWrapper {
    private final BaseScreen wrappedGui;
    private final TooltipList tooltipList;
    private boolean drawSlots = true;

    public MenuScreenWrapper(BaseScreen wrappedGui, T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.wrappedGui = wrappedGui;
        tooltipList = new TooltipList();
    }

    public MenuScreenWrapper<T> disableSlotDrawing() {
        drawSlots = false;
        return this;
    }

    @Override
    public void init() {
        super.init();
        wrappedGui.initGui();
        leftPos = wrappedGui.getX();
        topPos = wrappedGui.getY();
        imageWidth = wrappedGui.width;
        imageHeight = wrappedGui.height;
    }

    @Override
    public boolean isPauseScreen() {
        return wrappedGui.doesGuiPauseGame();
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        wrappedGui.updateMouseOver((int) x, (int) y);

        if (button == MouseButton.BACK.id) {
            wrappedGui.onBack();
            return true;
        } else {
            return wrappedGui.mousePressed(MouseButton.get(button)) || super.mouseClicked(x, y, button);
        }
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        wrappedGui.updateMouseOver((int) x, (int) y);
        wrappedGui.mouseReleased(MouseButton.get(button));
        return super.mouseReleased(x, y, button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dirX, double dirY) {
        return wrappedGui.mouseScrolled(dirY) || super.mouseScrolled(x, y, dirX, dirY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        var key = new Key(keyCode, scanCode, modifiers);

        if (wrappedGui.keyPressed(key)) {
            return true;
        } else {
            if (key.backspace()) {
                wrappedGui.onBack();
                return true;
            } else if (wrappedGui.onClosedByKey(key)) {
                if (shouldCloseOnEsc()) {
                    // false is important here; menu-based screens are driven by messages from the server,
                    //   so we can't just switch between screens
                    wrappedGui.closeGui(false);
                }
                return true;
            }

            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        var key = new Key(keyCode, scanCode, modifiers);
        wrappedGui.keyReleased(key);
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char keyChar, int modifiers) {
        if (wrappedGui.charTyped(keyChar, new KeyModifiers(modifiers))) {
            return true;
        }

        return super.charTyped(keyChar, keyChar);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float f, int mx, int my) {
        var theme = wrappedGui.getTheme();
        renderBackground(graphics, mx, my, f);
        wrappedGui.draw(graphics, theme, leftPos, topPos, imageWidth, imageHeight);

        if (drawSlots) {
            for (var slot : menu.slots) {
                theme.drawContainerSlot(graphics, leftPos + slot.x, topPos + slot.y, 16, 16);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(-leftPos, -topPos);

        var theme = wrappedGui.getTheme();
        wrappedGui.drawForeground(graphics, theme, leftPos, topPos, imageWidth, imageHeight);

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

        graphics.pose().popMatrix();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int x, int y, float partialTicks) {
        if (wrappedGui.drawDefaultBackground(graphics)) {
            super.renderBackground(graphics, x, y, partialTicks);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics, mouseX, mouseY, partialTicks);
        wrappedGui.updateGui(mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void containerTick() {
        super.containerTick();
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
}
