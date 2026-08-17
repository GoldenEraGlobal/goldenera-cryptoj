/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025-2030 The GoldenEraGlobal Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package global.goldenera.cryptoj.builder.payloads;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

import global.goldenera.cryptoj.builder.TxBuilder;
import global.goldenera.cryptoj.common.Tx;
import global.goldenera.cryptoj.common.payloads.bip.TxBipNetworkParamsSetPayload;
import global.goldenera.cryptoj.common.payloads.bip.TxBipValidatorAddPayload;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.enums.MiningLimitMode;
import global.goldenera.cryptoj.enums.Network;
import global.goldenera.cryptoj.enums.TxPayloadVersion;
import global.goldenera.cryptoj.enums.TxVersion;
import global.goldenera.cryptoj.exceptions.CryptoJException;
import global.goldenera.cryptoj.serialization.tx.payload.TxPayloadEncoder;

class PayloadBuilderVersionTest {

    private static final Address VALIDATOR = Address.fromHexString("0x1111111111111111111111111111111111111111");

    @Test
    void validatorAddDefaultsToV2AndRequiresPolicy() throws CryptoJException {
        assertThrows(IllegalStateException.class,
                () -> TxBuilder.create().addValidator().validator(VALIDATOR).done());

        Tx tx = TxBuilder.create()
                .addValidator()
                .validator(VALIDATOR)
                .miningPolicy(MiningLimitMode.LIMITED, 3_000)
                .done()
                .network(Network.TESTNET)
                .nonce(1)
                .buildUnsigned();
        TxBipValidatorAddPayload payload = (TxBipValidatorAddPayload) tx.getPayload();
        assertEquals(TxPayloadVersion.V2, payload.getPayloadVersion());
        assertEquals(Bytes.fromHexString("0xdb0a0294111111111111111111111111111111111111111180820bb8"),
                TxPayloadEncoder.INSTANCE.encode(payload, TxVersion.V1));
    }

    @Test
    void validatorAddLegacyV1IsExplicitAndAddressOnly() throws CryptoJException {
        Tx tx = TxBuilder.create()
                .addValidator()
                .validator(VALIDATOR)
                .legacyV1()
                .done()
                .network(Network.TESTNET)
                .nonce(1)
                .buildUnsigned();
        TxBipValidatorAddPayload payload = (TxBipValidatorAddPayload) tx.getPayload();
        assertEquals(TxPayloadVersion.V1, payload.getPayloadVersion());
        assertEquals(Bytes.fromHexString("0xd60a941111111111111111111111111111111111111111"),
                TxPayloadEncoder.INSTANCE.encode(payload, TxVersion.V1));

        assertThrows(IllegalStateException.class, () -> TxBuilder.create()
                .addValidator()
                .validator(VALIDATOR)
                .legacyV1()
                .miningPolicy(MiningLimitMode.UNLIMITED, 0));
        assertThrows(IllegalStateException.class, () -> TxBuilder.create()
                .addValidator()
                .validator(VALIDATOR)
                .miningPolicy(MiningLimitMode.UNLIMITED, 0)
                .legacyV1());
    }

    @Test
    void networkParamsDefaultsToV2WithoutRequiringResize() throws CryptoJException {
        Tx tx = TxBuilder.create()
                .setNetworkParams()
                .targetMiningTime(1L)
                .done()
                .network(Network.TESTNET)
                .nonce(1)
                .buildUnsigned();
        TxBipNetworkParamsSetPayload payload = (TxBipNetworkParamsSetPayload) tx.getPayload();
        assertEquals(TxPayloadVersion.V2, payload.getPayloadVersion());
        assertEquals(null, payload.getValidatorMiningWindowBlocks());
        assertEquals(null, payload.getMiningRewardVestingBlocks());
        assertEquals(Bytes.fromHexString("0xcc0402c0c0c101c0c0c0c0c0c0"),
                TxPayloadEncoder.INSTANCE.encode(payload, TxVersion.V1));
    }

    @Test
    void networkParamsLegacyV1RequiresExplicitOptInAndRejectsWindow() throws CryptoJException {
        Tx tx = TxBuilder.create()
                .setNetworkParams()
                .targetMiningTime(1L)
                .legacyV1()
                .done()
                .network(Network.TESTNET)
                .nonce(1)
                .buildUnsigned();
        TxBipNetworkParamsSetPayload payload = (TxBipNetworkParamsSetPayload) tx.getPayload();
        assertEquals(TxPayloadVersion.V1, payload.getPayloadVersion());
        assertEquals(Bytes.fromHexString("0xc904c0c0c101c0c0c0c0"),
                TxPayloadEncoder.INSTANCE.encode(payload, TxVersion.V1));

        assertThrows(IllegalStateException.class, () -> TxBuilder.create()
                .setNetworkParams()
                .legacyV1()
                .validatorMiningWindowBlocks(100));
        assertThrows(IllegalStateException.class, () -> TxBuilder.create()
                .setNetworkParams()
                .validatorMiningWindowBlocks(100)
                .legacyV1());
        assertThrows(IllegalStateException.class, () -> TxBuilder.create()
                .setNetworkParams()
                .legacyV1()
                .miningRewardVestingBlocks(1));
        assertThrows(IllegalStateException.class, () -> TxBuilder.create()
                .setNetworkParams()
                .miningRewardVestingBlocks(1)
                .legacyV1());
    }

    @Test
    void networkParamsV2AcceptsCanonicalRewardVestingBounds() throws CryptoJException {
        Tx tx = TxBuilder.create()
                .setNetworkParams()
                .miningRewardVestingBlocks(1_000_000)
                .done()
                .network(Network.TESTNET)
                .nonce(1)
                .buildUnsigned();
        TxBipNetworkParamsSetPayload payload = (TxBipNetworkParamsSetPayload) tx.getPayload();
        assertEquals(1_000_000L, payload.getMiningRewardVestingBlocks());

        assertThrows(RuntimeException.class, () -> TxBuilder.create()
                .setNetworkParams()
                .miningRewardVestingBlocks(-1));
        assertThrows(RuntimeException.class, () -> TxBuilder.create()
                .setNetworkParams()
                .miningRewardVestingBlocks(1_000_001));
    }
}
