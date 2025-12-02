# 🛡️ goldenera-cryptoj

`goldenera-cryptoj` is the central, **consensus-critical cryptographic library** for the **GOLDENERA** blockchain.

This library is a lightweight wrapper around standard, audited Java libraries, primarily leveraging utilities from `web3j` (for key/signature handling) and `org.apache.tuweni` (for hashing and canonical bytes manipulation).

---

## Consensus Standards

This library enforces the following cryptographic and derivation standards, which are fundamental to the goldenera network consensus:

- **Hashing:** **Keccak-256** (used for all blocks, transactions, and Merkle Tries).
- **Serialization:** **RLP (Recursive Length Prefix)** (used for encoding canonical blocks, transactions, and state entries).
- **Elliptic Curve:** **secp256k1** (Standard used by Bitcoin and Ethereum).
- **Key Derivation:** **BIP-39 (Mnemonic) + BIP-44**.
- **Derivation Path:** `m/44'/60'/0'/0/x` (The standard Ethereum path to ensure compatibility with modern wallet standards).
- **Addresses:** **20 bytes** derived from the last 20 bytes of `Keccak-256(pubkey)`, and encoded using **EIP-55 checksum**.
- **Signatures:** **65-byte (r, s, v)** format, crucial for public key recovery (**ecrecover**).

---

## 🛠️ Library Usage (Demo)

Before running the demo, ensure the Bouncy Castle provider is correctly configured:

### 1. Dependency

Add the Bouncy Castle dependency to your project:

```xml
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk18on</artifactId>
    <version>1.81</version>
</dependency>
```

Register the provider statically in your application's entry point or configuration:

```java
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

static {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
        Security.addProvider(new BouncyCastleProvider());
    }
}
```

This example demonstrates the full flow from mnemonic generation to signature verification:

```java
import org.apache.tuweni.bytes.Bytes;

import global.goldenera.cryptoj.datatypes.Address;
import global.goldenera.cryptoj.datatypes.Hash;
import global.goldenera.cryptoj.datatypes.PrivateKey;
import global.goldenera.cryptoj.datatypes.Signature;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CryptoJDemo {

	static {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	public static void main(String[] args) throws Exception {

		// === 1. Mnemonic and Key Generation ===
		String mnemonic = PrivateKey.generateMnemonic();
		String password = "myStrongPassword";

		// Derive the first account (index 0)
		PrivateKey keyPair_Account1 = PrivateKey.load(mnemonic, password, 0);

		// Derive the second account (index 1)
		PrivateKey keyPair_Account2 = PrivateKey.load(mnemonic, password, 1);

		// === 2. Address Derivation ===
		Address address1 = Address.fromKeyPair(keyPair_Account1);
		Address address2 = Address.fromKeyPair(keyPair_Account2);

		// Encode for display (EIP-55 Checksum)
		System.out.println("Account 1: " + address1.toChecksumAddress());
		System.out.println("Account 2: " + address2.toChecksumAddress());

		// === 3. Signing a Message (Transaction) ===
		// (This would be the RLP-encoded bytes of your transaction)
		Bytes transactionData = Bytes.wrap("Test Data".getBytes());

		// 3a. Hashing (Keccak-256)
		Hash txHash = Hash.hash(transactionData);
		System.out.println("\nHash to sign: " + txHash.toHexString());

		// 3b. Signing
		Signature signature = keyPair_Account1.sign(txHash);

		// === 4. Verifying the Signature (Node-side) ===
		System.out.println("Verifying signature...");

		// 4a. Recover the address from the signature
		Address recoveredAddress = signature.recoverAddress(txHash);

		// 4b. Validate
		boolean isValid = signature.validate(txHash, address1);

		if (isValid && address1.equals(recoveredAddress)) {
			System.out.println(
					"RESULT: Signature is valid. Sender: " + recoveredAddress.toChecksumAddress());
		} else {
			System.out.println("RESULT: Signature is INVALID.");
		}
	}
}
```

## Credits
- [Web3j](https://www.web3labs.com/web3j-sdk)
- [Apache Tuweni](https://github.com/consensys/tuweni)
- [BouncyCastle](https://www.bouncycastle.org/)

## License
This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.