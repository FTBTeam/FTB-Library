package dev.ftb.mods.ftblibrary.ui.misc;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.FluidStackHooks;
import dev.architectury.platform.Platform;
import dev.ftb.mods.ftblibrary.config.*;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.config.ui.SelectItemStackScreen;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.ContextMenu;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

public class UITesting {
    public static void openTestScreen() {
        var group = new ConfigGroup("test", accepted ->
                Minecraft.getInstance().player.displayClientMessage(Component.literal("Accepted: " + accepted), false));
        group.add("image", new ImageResourceConfig(), ImageResourceConfig.NONE, v -> {
        }, ImageResourceConfig.NONE);

        group.addItemStack("itemstack", ItemStack.EMPTY, v -> {
        }, ItemStack.EMPTY, false, true);
        group.addItemStack("item", ItemStack.EMPTY, v -> {
        }, ItemStack.EMPTY, 1).setAllowNBTEdit(false);
        group.addFluidStack("fluidstack", FluidStack.empty(), v -> {
        }, FluidStack.empty(), true);
        FluidStack water = FluidStack.create(Fluids.WATER, FluidStackHooks.bucketAmount());
        group.addFluidStack("fluid", water, v -> {
        }, water, water.getAmount()).showAmount(false).setAllowNBTEdit(false);

        ConfigGroup grp1 = group.getOrCreateSubgroup("group1");
        grp1.addInt("integer", 1, v -> {
        }, 0, 0, 10);
        grp1.addLong("long", 10L, v -> {
        }, 0L, 0L, 1000L);
        grp1.addDouble("double", 1.5, v -> {
        }, 0.0, -10.0, 10.0);
        grp1.addBool("bool", true, v -> {
        }, false);
        grp1.addString("string", "some text", v -> {
        }, "");

        ConfigGroup grp2 = grp1.getOrCreateSubgroup("subgroup1");
        grp2.addEnum("enum", Direction.UP, v -> {
        }, NameMap.of(Direction.UP, Direction.values()).create());
        List<Integer> integers = new ArrayList<>(List.of(1, 2, 3, 4));
        grp2.addList("int_list", integers, new IntConfig(0, 10), 1);
        List<String> strings = new ArrayList<>(List.of("line one", "line two", "line three"));
        grp2.addList("str_list", strings, new StringConfig(), "");

        ConfigGroup grp3 = grp2.getOrCreateSubgroup("subgroup2");
        grp3.addColor("color", Color4I.WHITE, v -> {
        }, Color4I.GRAY);
        grp3.addColor("color_alpha", Color4I.WHITE, v -> {
        }, Color4I.GRAY).withAlphaEditing();
        grp3.addItemStack("itemstack", ItemStack.EMPTY, v -> {
        }, ItemStack.EMPTY, false, false);

        new TestConfigScreen(group).setAutoclose(true).openGuiLater();
    }

    public static class TestConfigScreen extends EditConfigScreen {
        public TestConfigScreen(ConfigGroup configGroup) {
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
                    new ContextMenuItem(Component.literal("Select item"), Icons.ADD, button -> {
                        new SelectItemStackScreen(new ItemStackConfig(1), accepted -> parent.getGui().run()).openGui();
                    }),
                    ContextMenuItem.subMenu(Component.literal("line 2 >"), Icons.REMOVE, List.of(
                            ContextMenuItem.title(Component.literal("Submenu")),
                            ContextMenuItem.SEPARATOR,
                            new ContextMenuItem(Component.literal("line 2a"), Icons.FRIENDS_GROUP, button1 -> {
                            }
                            ))),
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
                if (Platform.isDevelopmentEnvironment() && mouseButton.isRight()) {
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
