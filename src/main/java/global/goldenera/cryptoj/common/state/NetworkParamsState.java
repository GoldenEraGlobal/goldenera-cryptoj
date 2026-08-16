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
package global.goldenera.cryptoj.common.state;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.ethereum.Wei;

import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.datatypes.Hash;
import global.goldenera.cryptoj.enums.state.NetworkParamsStateVersion;

public interface NetworkParamsState {

    public static final Bytes KEY = Bytes.wrap("SINGLETON_PARAMS".getBytes(StandardCharsets.UTF_8));

    NetworkParamsStateVersion getVersion();

    Wei getBlockReward();

    Address getBlockRewardPoolAddress();

    long getTargetMiningTimeMs();

    long getAsertHalfLifeBlocks();

    long getAsertAnchorHeight();

    BigInteger getMinDifficulty();

    Wei getMinTxBaseFee();

    Wei getMinTxByteFee();

    Hash getUpdatedByTxHash();

    long getCurrentAuthorityCount();

    long getCurrentValidatorCount();

    /** Legacy V1 states treat every registered validator as UNLIMITED. */
    default long getCurrentUnlimitedValidatorCount() {
        return getCurrentValidatorCount();
    }

    /** Zero means this parameter is absent from a legacy V1 state. */
    default long getValidatorMiningWindowBlocks() {
        return 0;
    }

    /** Zero means mining rewards are immediately spendable. */
    default long getMiningRewardVestingBlocks() {
        return 0;
    }

    /** Sorted multiset of BPS values for all active LIMITED validators. */
    default List<Long> getLimitedValidatorMiningSharesBps() {
        return List.of();
    }

    long getUpdatedAtBlockHeight();

    Instant getUpdatedAtTimestamp();
}
