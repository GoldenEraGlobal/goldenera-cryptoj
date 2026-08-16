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
package global.goldenera.cryptoj.serialization.state.miningrewardmaturity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.ethereum.Wei;
import org.junit.jupiter.api.Test;

import global.goldenera.cryptoj.common.state.MiningRewardMaturityState;
import global.goldenera.cryptoj.common.state.impl.MiningRewardMaturityStateImpl;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.enums.state.MiningRewardMaturityStateVersion;

class MiningRewardMaturityStateSerializationTest {

    private static final Address FIRST = Address.fromHexString("0x1111111111111111111111111111111111111111");
    private static final Address SECOND = Address.fromHexString("0x2222222222222222222222222222222222222222");

    @Test
    void roundTripsCanonicalAddressSortedEncodingIndependentOfInsertionOrder() {
        Map<Address, Wei> reverseInsertionOrder = new LinkedHashMap<>();
        reverseInsertionOrder.put(SECOND, Wei.valueOf(2));
        reverseInsertionOrder.put(FIRST, Wei.valueOf(1));
        MiningRewardMaturityState state = new MiningRewardMaturityStateImpl(
                MiningRewardMaturityStateVersion.V1, reverseInsertionOrder);
        Bytes expected = Bytes.fromHexString("0xf001eed694111111111111111111111111111111111111111101"
                + "d694222222222222222222222222222222222222222202");

        Bytes encoded = MiningRewardMaturityStateEncoder.INSTANCE.encode(state);
        assertEquals(expected, encoded);
        assertEquals(state, MiningRewardMaturityStateDecoder.INSTANCE.decode(encoded));
    }

    @Test
    void aggregatesRewardsForSameBeneficiaryAndSupportsEmptySentinel() {
        MiningRewardMaturityStateImpl state = MiningRewardMaturityStateImpl.empty()
                .addReward(FIRST, Wei.valueOf(2))
                .addReward(FIRST, Wei.valueOf(3));
        assertEquals(Map.of(FIRST, Wei.valueOf(5)), state.getRewards());
        assertEquals(state, MiningRewardMaturityStateDecoder.INSTANCE.decode(
                MiningRewardMaturityStateEncoder.INSTANCE.encode(state)));
        assertEquals(MiningRewardMaturityStateImpl.ZERO,
                MiningRewardMaturityStateDecoder.INSTANCE.decode(
                        MiningRewardMaturityStateEncoder.INSTANCE.encode(MiningRewardMaturityStateImpl.ZERO)));
    }

    @Test
    void rejectsNonPositiveAndNonCanonicalEntries() {
        assertThrows(IllegalArgumentException.class,
                () -> MiningRewardMaturityStateImpl.empty().addReward(FIRST, Wei.ZERO));

        Bytes unsorted = Bytes.fromHexString("0xf001eed694222222222222222222222222222222222222222202"
                + "d694111111111111111111111111111111111111111101");
        assertThrows(RuntimeException.class, () -> MiningRewardMaturityStateDecoder.INSTANCE.decode(unsorted));

        Bytes zeroAmount = Bytes.fromHexString("0xd901d7d694111111111111111111111111111111111111111180");
        assertThrows(RuntimeException.class, () -> MiningRewardMaturityStateDecoder.INSTANCE.decode(zeroAmount));
    }
}
