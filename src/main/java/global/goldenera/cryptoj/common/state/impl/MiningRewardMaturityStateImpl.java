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
package global.goldenera.cryptoj.common.state.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.tuweni.units.ethereum.Wei;

import global.goldenera.cryptoj.common.MiningRewardMaturityStateValidation;
import global.goldenera.cryptoj.common.state.MiningRewardMaturityState;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.enums.state.MiningRewardMaturityStateVersion;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public final class MiningRewardMaturityStateImpl implements MiningRewardMaturityState {

	public static final MiningRewardMaturityState ZERO = empty();

	private final MiningRewardMaturityStateVersion version;
	private final Map<Address, Wei> rewards;

	public MiningRewardMaturityStateImpl(MiningRewardMaturityStateVersion version, Map<Address, Wei> rewards) {
		this.version = version;
		this.rewards = Collections.unmodifiableMap(new LinkedHashMap<>(rewards == null ? Map.of() : rewards));
		MiningRewardMaturityStateValidation.validate(this);
	}

	public static MiningRewardMaturityStateImpl empty() {
		return new MiningRewardMaturityStateImpl(MiningRewardMaturityStateVersion.V1, Map.of());
	}

	public MiningRewardMaturityStateImpl addReward(Address address, Wei amount) {
		if (address == null || amount == null || amount.compareTo(Wei.ZERO) <= 0) {
			throw new IllegalArgumentException("Mining reward maturity amount must be positive");
		}
		Map<Address, Wei> updated = new LinkedHashMap<>(rewards);
		updated.merge(address, amount, Wei::addExact);
		return new MiningRewardMaturityStateImpl(version, updated);
	}
}
