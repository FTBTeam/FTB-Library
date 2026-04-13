package dev.ftb.mods.ftblibrary.neoforge.platform.transfer;

import dev.ftb.mods.ftblibrary.neoforge.platform.transfer.simple.NeoSimpleTransferImpl;
import dev.ftb.mods.ftblibrary.platform.transfer.Transfer;
import dev.ftb.mods.ftblibrary.platform.transfer.simple.SimpleTransfer;

public class NeoTransferImpl implements Transfer {
    private final SimpleTransfer simpleTransfer = new NeoSimpleTransferImpl();

    @Override
    public SimpleTransfer simple() {
        return simpleTransfer;
    }
}
