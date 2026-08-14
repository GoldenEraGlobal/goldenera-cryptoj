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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import global.goldenera.cryptoj.common.MiningConsensusRules;
import global.goldenera.cryptoj.common.MiningWindowStateValidation;
import global.goldenera.cryptoj.common.state.MiningWindowState;
import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.enums.state.MiningWindowStateVersion;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public final class MiningWindowStateImpl implements MiningWindowState {

	public static final MiningWindowState ZERO = new MiningWindowStateImpl(
			MiningWindowStateVersion.V1, 0, List.of(), Map.of(), 0);

	private final MiningWindowStateVersion version;
	private final long windowSize;
	private final List<Address> orderedValidatorIdentities;
	private final Map<Address, Long> validatorBlockCounts;
	private final long lastUpdatedBlockHeight;

	@Builder(toBuilder = true)
	public MiningWindowStateImpl(MiningWindowStateVersion version, long windowSize,
			List<Address> orderedValidatorIdentities, Map<Address, Long> validatorBlockCounts,
			long lastUpdatedBlockHeight) {
		this.version = version;
		this.windowSize = windowSize;
		this.orderedValidatorIdentities = Collections.unmodifiableList(
				new ArrayList<>(orderedValidatorIdentities == null ? List.of() : orderedValidatorIdentities));
		this.validatorBlockCounts = Collections.unmodifiableMap(
				new LinkedHashMap<>(validatorBlockCounts == null ? Map.of() : validatorBlockCounts));
		this.lastUpdatedBlockHeight = lastUpdatedBlockHeight;
	}

	public static MiningWindowStateImpl empty(long windowSize, long lastUpdatedBlockHeight) {
		MiningConsensusRules.validateWindowSize(windowSize);
		return new MiningWindowStateImpl(MiningWindowStateVersion.V1, windowSize, List.of(), Map.of(),
				lastUpdatedBlockHeight);
	}

	/** Returns a new state after atomically appending and, when full, evicting. */
	public MiningWindowStateImpl append(Address validatorIdentity, long blockHeight) {
		MiningConsensusRules.validateWindowSize(windowSize);
		MiningWindowStateValidation.validate(this);
		if (validatorIdentity == null) {
			throw new IllegalArgumentException("Validator identity cannot be null");
		}
		List<Address> ordered = new ArrayList<>(orderedValidatorIdentities);
		Map<Address, Long> counts = new LinkedHashMap<>(validatorBlockCounts);
		if (ordered.size() == windowSize) {
			Address evicted = ordered.removeFirst();
			long remaining = counts.getOrDefault(evicted, 0L) - 1;
			if (remaining == 0) {
				counts.remove(evicted);
			} else {
				counts.put(evicted, remaining);
			}
		}
		ordered.add(validatorIdentity);
		counts.merge(validatorIdentity, 1L, Long::sum);
		return new MiningWindowStateImpl(version, windowSize, ordered, counts, blockHeight);
	}
}
