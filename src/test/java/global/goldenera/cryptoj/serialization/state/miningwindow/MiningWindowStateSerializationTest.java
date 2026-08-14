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
package global.goldenera.cryptoj.serialization.state.miningwindow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

import global.goldenera.cryptoj.common.state.MiningWindowState;
import global.goldenera.cryptoj.common.state.impl.MiningWindowStateImpl;
import global.goldenera.cryptoj.datatypes.Address;

class MiningWindowStateSerializationTest {

	private static final Address A = Address.fromHexString("0x1111111111111111111111111111111111111111");
	private static final Address B = Address.fromHexString("0x2222222222222222222222222222222222222222");

	@Test
	void zeroValueIsSafeAndRoundTrips() {
		Bytes encoded = MiningWindowStateEncoder.INSTANCE.encode(MiningWindowStateImpl.ZERO);
		assertEquals(MiningWindowStateImpl.ZERO, MiningWindowStateDecoder.INSTANCE.decode(encoded));
	}

	@Test
	void appendEvictsAtomicallyAndRoundTripsDeterministically() {
		MiningWindowStateImpl state = MiningWindowStateImpl.empty(100, 9);
		for (int i = 0; i < 100; i++) {
			state = state.append(i == 0 ? A : B, 10 + i);
		}
		assertEquals(1, state.getValidatorBlockCounts().get(A));
		state = state.append(B, 110);
		assertFalse(state.getValidatorBlockCounts().containsKey(A));
		assertEquals(100, state.getValidatorBlockCounts().get(B));

		Bytes encoded = MiningWindowStateEncoder.INSTANCE.encode(state);
		MiningWindowState decoded = MiningWindowStateDecoder.INSTANCE.decode(encoded);
		assertEquals(state, decoded);
		assertEquals(encoded, MiningWindowStateEncoder.INSTANCE.encode(decoded));
	}
}
