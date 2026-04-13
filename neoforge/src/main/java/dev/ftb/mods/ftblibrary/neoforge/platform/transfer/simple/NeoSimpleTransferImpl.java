package dev.ftb.mods.ftblibrary.neoforge.platform.transfer.simple;

import dev.ftb.mods.ftblibrary.platform.transfer.simple.BlockEntityTransfer;
import dev.ftb.mods.ftblibrary.platform.transfer.simple.SimpleTransfer;

public class NeoSimpleTransferImpl implements SimpleTransfer {
    private final BlockEntityTransfer blockEntityTransfer = new NeoBlockEntityTransferImpl();

    @Override
    public BlockEntityTransfer blockEntity() {
        return blockEntityTransfer;
    }
}
