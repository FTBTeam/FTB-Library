package dev.ftb.mods.ftblibrary.fabric.platform.transfer;

import dev.ftb.mods.ftblibrary.fabric.platform.transfer.simple.FabricSimpleTransferImpl;
import dev.ftb.mods.ftblibrary.platform.transfer.Transfer;
import dev.ftb.mods.ftblibrary.platform.transfer.simple.SimpleTransfer;

public class FabricTransferImpl implements Transfer {
    private final SimpleTransfer simpleTransfer = new FabricSimpleTransferImpl();

    @Override
    public SimpleTransfer simple() {
        return simpleTransfer;
    }
}
