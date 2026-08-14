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
package global.goldenera.cryptoj.common;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import global.goldenera.cryptoj.enums.MiningLimitMode;
import global.goldenera.cryptoj.enums.TxPayloadType;
import global.goldenera.cryptoj.enums.TxPayloadVersion;
import global.goldenera.cryptoj.enums.state.BipType;
import global.goldenera.cryptoj.enums.state.NetworkParamsStateVersion;
import global.goldenera.cryptoj.enums.state.ValidatorStateVersion;

class MiningConsensusRulesTest {

	@Test
	void acceptsCanonicalBoundariesAndRejectsOutsideValues() {
		assertDoesNotThrow(() -> MiningConsensusRules.validatePolicy(MiningLimitMode.LIMITED, 1));
		assertDoesNotThrow(() -> MiningConsensusRules.validatePolicy(MiningLimitMode.LIMITED, 4_000));
		assertDoesNotThrow(() -> MiningConsensusRules.validatePolicy(MiningLimitMode.UNLIMITED, 0));
		assertThrows(RuntimeException.class,
				() -> MiningConsensusRules.validatePolicy(MiningLimitMode.LIMITED, 0));
		assertThrows(RuntimeException.class,
				() -> MiningConsensusRules.validatePolicy(MiningLimitMode.LIMITED, 4_001));
		assertThrows(RuntimeException.class,
				() -> MiningConsensusRules.validatePolicy(MiningLimitMode.UNLIMITED, 1));

		assertDoesNotThrow(() -> MiningConsensusRules.validateWindowSize(100));
		assertDoesNotThrow(() -> MiningConsensusRules.validateWindowSize(10_000));
		assertThrows(RuntimeException.class, () -> MiningConsensusRules.validateWindowSize(99));
		assertThrows(RuntimeException.class, () -> MiningConsensusRules.validateWindowSize(10_001));
	}

	@Test
	void protocolNumericCodesAreStable() {
		assertEquals(0, MiningLimitMode.LIMITED.getCode());
		assertEquals(1, MiningLimitMode.UNLIMITED.getCode());
		assertEquals(1, TxPayloadVersion.V1.getCode());
		assertEquals(2, TxPayloadVersion.V2.getCode());
		assertEquals(1, ValidatorStateVersion.V1.getCode());
		assertEquals(2, ValidatorStateVersion.V2.getCode());
		assertEquals(1, NetworkParamsStateVersion.V1.getCode());
		assertEquals(2, NetworkParamsStateVersion.V2.getCode());
		assertEquals(12, TxPayloadType.BIP_VALIDATOR_MINING_POLICY_SET.getCode());
		assertEquals(11, BipType.VALIDATOR_MINING_POLICY_SET.getCode());
	}
}
