package dev.ftb.mods.ftblibrary.client.gui;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.client.config.EditableConfigGroup;
import dev.ftb.mods.ftblibrary.client.config.Tristate;
import dev.ftb.mods.ftblibrary.client.config.editable.*;
import dev.ftb.mods.ftblibrary.client.config.gui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.client.config.gui.resource.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.screens.AbstractButtonListScreen;
import dev.ftb.mods.ftblibrary.client.gui.widget.ContextMenu;
import dev.ftb.mods.ftblibrary.client.gui.widget.ContextMenuItem;
import dev.ftb.mods.ftblibrary.client.gui.widget.Panel;
import dev.ftb.mods.ftblibrary.client.gui.widget.SimpleTextButton;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.platform.fluid.FluidStack;
import dev.ftb.mods.ftblibrary.util.ModUtils;
import dev.ftb.mods.ftblibrary.util.NameMap;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

public class UITesting {
    public static void openTestScreen() {
        var group = new EditableConfigGroup("test", accepted ->
                ClientUtils.getClientPlayer().sendSystemMessage(Component.literal("Accepted: " + accepted)));
        group.add("image", new EditableImageResource(), EditableImageResource.NONE, UITesting::onChanged, EditableImageResource.NONE);

        group.addItemStack("itemstack", ItemStack.EMPTY, UITesting::onChanged, ItemStack.EMPTY, false, true);
        group.addItemStack("item", ItemStack.EMPTY, UITesting::onChanged, ItemStack.EMPTY, 1).setAllowNBTEdit(false);
        group.addItemStack("itemstack(blocks)", ItemStack.EMPTY, UITesting::onChanged, ItemStack.EMPTY, 1).withFilter(s -> s.getItem() instanceof BlockItem).setAllowNBTEdit(false);
        group.addFluidStack("fluidstack", FluidStack.EMPTY, UITesting::onChanged, FluidStack.EMPTY, true);
        FluidStack water = new FluidStack(Fluids.WATER, FluidStack.bucketFluidAmount());
        group.addFluidStack("fluid", water, UITesting::onChanged, water, water.amount()).showAmount(false).setAllowNBTEdit(false);
        group.addEntityFace("face", EditableEntityFace.NONE, UITesting::onChanged, EditableEntityFace.NONE);

        EditableConfigGroup grp1 = group.getOrCreateSubgroup("group1");
        grp1.addInt("integer", 1, UITesting::onChanged, 0, 0, 10);
        grp1.addLong("long", 10L, UITesting::onChanged, 0L, 0L, 1000L);
        grp1.addDouble("double", 1.5, UITesting::onChanged, 0.0, -10.0, 10.0);
        grp1.addBool("bool", true, UITesting::onChanged, false);
        grp1.addString("string", "some text", UITesting::onChanged, "");
        grp1.addTristate("tristate", Tristate.DEFAULT, UITesting::onChanged);

        EditableConfigGroup grp2 = grp1.getOrCreateSubgroup("subgroup1");
        grp2.addEnum("enum", Direction.UP, UITesting::onChanged, NameMap.of(Direction.UP, Direction.values()).create());
        List<Integer> integers = new ArrayList<>(List.of(1, 2, 3, 4));
        grp2.addList("int_list", integers, new EditableInt(0, 10), 1);
        List<String> strings = new ArrayList<>(List.of("line one", "line two", "line three"));
        grp2.addList("str_list", strings, new EditableString(), "");

        EditableConfigGroup grp3 = grp2.getOrCreateSubgroup("subgroup2");
        grp3.addColor("color", Color4I.WHITE, UITesting::onChanged, Color4I.GRAY);
        grp3.addColor("color_alpha", Color4I.WHITE, UITesting::onChanged, Color4I.GRAY).withAlphaEditing();
        grp3.addItemStack("itemstack", ItemStack.EMPTY, UITesting::onChanged, ItemStack.EMPTY, false, false);

        new TestConfigScreen(group).setAutoclose(true).openGuiLater();
    }

    private static void onChanged(Object o) {
        FTBLibrary.LOGGER.info("changed config val: {}", o);
    }

    public static class TestConfigScreen extends EditConfigScreen {
        public TestConfigScreen(EditableConfigGroup configGroup) {
            super(configGroup);
        }

        @Override
        protected Panel createTopPanel() {
            return new TestTopPanel();
        }

        private void openTestContextMenu(Panel parent) {
            ContextMenu menu = new ContextMenu(parent, List.of(
                    ContextMenuItem.title(Component.literal("Title")),
                    ContextMenuItem.SEPARATOR,
                    new ContextMenuItem(Component.literal("Select item"), Icons.ADD,
                            button -> new SelectItemStackScreen(new EditableItemStack(1), accepted -> parent.getGui().run()).openGui()
                    ),
                    ContextMenuItem.subMenu(Component.literal("line 2 >"), Icons.REMOVE, List.of(
                                    ContextMenuItem.title(Component.literal("Submenu")),
                                    ContextMenuItem.SEPARATOR,
                                    new ContextMenuItem(Component.literal("line 2a"), Icons.FRIENDS_GROUP, button1 -> {})
                            )
                    ),
                    new ContextMenuItem(Component.literal("Test Search"), Icon.empty(), button -> openTestButtonList()) {
                        @Override
                        public void addMouseOverText(TooltipList list) {
                            list.add(Component.literal("A test tooltip"));
                        }
                    }
            ));
            parent.getGui().openContextMenu(menu);
        }

        private void openTestButtonList() {
            TestButtonListScreen screen = new TestButtonListScreen(this);
            screen.setTitle(Component.literal("Test Search List"));
            screen.openGui();
        }

        private class TestTopPanel extends CustomTopPanel {
            @Override
            public boolean mousePressed(MouseButton mouseButton) {
                if (ModUtils.isDevMode() && mouseButton.isRight()) {
                    openTestContextMenu(this);
                }
                return super.mousePressed(mouseButton);
            }
        }
    }

    private static class TestButtonListScreen extends AbstractButtonListScreen {
        private final TestConfigScreen parent;

        public TestButtonListScreen(TestConfigScreen parent) {
            this.parent = parent;

            setBorder(2, 2, 1);
            showBottomPanel(false);
            setHasSearchBox(true);
        }

        @Override
        public void addButtons(Panel panel) {
            for (int i = 0; i < 50; i++) {
                panel.add(new SimpleTextButton(panel, Component.literal("Button " + i), Icons.ACCEPT) {
                    @Override
                    public void onClicked(MouseButton button) {
                        SimpleToast.info(Component.literal("Selected " + getTitle().getString()), Component.literal(" "));
                        closeGui();
                    }
                });
            }
        }

        @Override
        protected void doCancel() {
            parent.run();
        }

        @Override
        protected void doAccept() {
            parent.run();
        }
    }
}
