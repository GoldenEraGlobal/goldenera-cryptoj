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
package global.goldenera.cryptoj.serialization.state.miningrewardmaturity.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.tuweni.units.ethereum.Wei;

import global.goldenera.cryptoj.common.state.MiningRewardMaturityState;
import global.goldenera.cryptoj.common.state.impl.MiningRewardMaturityStateImpl;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.enums.state.MiningRewardMaturityStateVersion;
import global.goldenera.cryptoj.exceptions.CryptoJFailedException;
import global.goldenera.cryptoj.serialization.state.miningrewardmaturity.MiningRewardMaturityStateDecodingStrategy;
import global.goldenera.rlp.RLPInput;

public class MiningRewardMaturityStateV1DecodingStrategy implements MiningRewardMaturityStateDecodingStrategy {

    @Override
    public MiningRewardMaturityState decode(RLPInput input) {
        int entryCount = input.enterList();
        Map<Address, Wei> rewards = new LinkedHashMap<>();
        String previousAddress = null;
        for (int i = 0; i < entryCount; i++) {
            if (input.enterList() != 2) {
                throw new CryptoJFailedException("Mining reward maturity entry must have exactly two fields");
            }
            Address address = Address.wrap(input.readBytes());
            String addressHex = address.toHexString();
            if (previousAddress != null && previousAddress.compareTo(addressHex) >= 0) {
                throw new CryptoJFailedException("Mining reward maturity entries must use canonical address order");
            }
            Wei amount = Wei.valueOf(input.readBigIntegerScalar());
            if (amount.compareTo(Wei.ZERO) <= 0 || rewards.put(address, amount) != null) {
                throw new CryptoJFailedException("Mining reward maturity amounts must be positive and unique");
            }
            previousAddress = addressHex;
            input.leaveList();
        }
        input.leaveList();
        return new MiningRewardMaturityStateImpl(MiningRewardMaturityStateVersion.V1, rewards);
    }
}
