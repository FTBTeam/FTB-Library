package dev.ftb.mods.ftblibrary.fabric.platform.transfer.simple;

import dev.ftb.mods.ftblibrary.platform.transfer.simple.BlockEntityTransfer;
import dev.ftb.mods.ftblibrary.platform.transfer.simple.SimpleTransfer;

public class FabricSimpleTransferImpl implements SimpleTransfer {
    private final BlockEntityTransfer blockEntityTransfer = new FabricBlockEntityTransferImpl();

    @Override
    public BlockEntityTransfer blockEntity() {
        return blockEntityTransfer;
    }
}
