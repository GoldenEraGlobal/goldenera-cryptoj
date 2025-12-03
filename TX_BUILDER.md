# TxBuilder - Transaction Builder for Wallet Applications

## Overview

`TxBuilder` is a user-friendly, fluent API for building and signing transactions in the GoldenEra CryptoJ library. It's specifically designed for wallet applications, providing an intuitive interface for creating blockchain transactions with compile-time safety and sensible defaults.

## Features

✅ **Fluent API** - Method chaining for clean, readable code  
✅ **Type-Safe** - Compile-time validation prevents common mistakes  
✅ **Sensible Defaults** - Auto-fills version, timestamp, zero amounts  
✅ **Simple Signing** - One-step `.sign(privateKey)` method  
✅ **Validation** - Built-in checks for required fields  
✅ **Hardware Wallet Support** - Build unsigned transactions  
✅ **Transaction Preview** - Compute hash and estimate size before signing  

## Quick Start

### Simple Transfer

```java
import global.goldenera.cryptoj.builder.TxBuilder;
import global.goldenera.cryptoj.common.Tx;
import global.goldenera.cryptoj.utils.Amounts;

PrivateKey myKey = PrivateKey.load(mnemonic, password, 0);
Address recipientAddress = Address.fromHexString("0x...");

Tx tx = TxBuilder.create()
    .type(TxType.TRANSFER)
    .network(Network.MAINNET)
    .recipient(recipientAddress)
    .amount(Amounts.tokensDecimal("1.5"))
    .fee(Amounts.Fees.STANDARD)
    .nonce(1L)
    .sign(myKey);                       // Sign in one step!
```

### Transfer with Message

```java
Tx tx = TxBuilder.create()
    .type(TxType.TRANSFER)
    .network(Network.MAINNET)
    .recipient(recipientAddress)
    .amount(Amounts.tokensDecimal("0.5"))
    .fee(Amounts.Fees.STANDARD)
    .nonce(2L)
    .message("Payment for invoice #123")  // UTF-8 string
    .sign(myKey);
```

### BIP Token Create

```java
Tx tx = TxBuilder.create()
    .tokenCreate()
        .name("MyToken")
        .symbol("MTK")
        .decimals(18)
        .website("https://mytoken.io")
        .maxSupply(BigInteger.valueOf(1_000_000_000))
    .done()
    .network(Network.MAINNET)
    .nonce(3L)
    .fee(Amounts.Fees.BIP)
    .sign(myKey);
```

### Token Mint

```java
Tx tx = TxBuilder.create()
    .tokenMint()
        .forProposal(bipCreateTxHash)  // Reference to approved proposal
        .token(tokenAddress)
        .recipient(recipientAddress)
        .amount(Amounts.tokens(1000))
        .minerFee(Amounts.Fees.STANDARD)
    .done()
    .network(Network.MAINNET)
    .nonce(4L)
    .fee(Amounts.tokensDecimal("0.005"))
    .sign(myKey);
```

## Advanced Usage

### Preview Hash Before Signing

```java
TxBuilder builder = TxBuilder.create()
    .type(TxType.TRANSFER)
    .network(Network.MAINNET)
    .recipient(recipientAddress)
    .amount(Amounts.tokens(2))
    .nonce(5L);

// Preview what will be signed
Hash hash = builder.computeHash();
System.out.println("Hash to sign: " + hash.toHexString());

// Estimate transaction size
int size = builder.estimateSize();
System.out.println("Estimated size: " + size + " bytes");

// Now sign
Tx tx = builder.sign(myKey);
```

### Build Unsigned (for Hardware Wallets)

```java
// Build unsigned transaction
Tx unsignedTx = TxBuilder.create()
    .type(TxType.TRANSFER)
    .network(Network.MAINNET)
    .recipient(recipientAddress)
    .amount(Amounts.tokens(5))
    .fee(Amounts.tokensDecimal("0.002"))
    .nonce(6L)
    .buildUnsigned();

// Get hash for hardware wallet to sign
Hash hashForHardware = TxUtil.hashTxForSigning(unsignedTx);

// Send hash to hardware wallet...
// Receive signature from hardware wallet...

// Manually add signature to create signed transaction
Tx signedTx = TxImpl.builder()
    .version(unsignedTx.getVersion())
    .timestamp(unsignedTx.getTimestamp())
    // ... copy all fields
    .signature(hardwareSignature)
    .build();
```

### Custom Timestamp

```java
Instant customTime = Instant.parse("2025-01-01T00:00:00Z");

Tx tx = TxBuilder.create()
    .type(TxType.TRANSFER)
    .network(Network.TESTNET)
    .recipient(recipientAddress)
    .amount(Amounts.tokens(1))
    .nonce(7L)
    .timestamp(customTime)
    .sign(myKey);
```

## Available Methods

### Required Fields

| Method | Description | Default |
|--------|-------------|---------|
| `.type(TxType)` | Transaction type (TRANSFER, BIP_CREATE, etc.) | - |
| `.network(Network)` | MAINNET or TESTNET | - |
| `.nonce(long)` | Account sequence number | - |

### Optional Fields

| Method | Description | Default |
|--------|-------------|---------|
| `.version(TxVersion)` | Transaction version | `TxVersion.V1` |
| `.timestamp(Instant)` | Transaction timestamp | `Instant.now()` |
| `.recipient(Address)` | Recipient address (required for TRANSFER) | `null` |
| `.amount(Wei)` | Amount to transfer | `Wei.ZERO` |
| `.fee(Wei)` | Transaction fee | `Wei.ZERO` |
| `.tokenAddress(Address)` | Token contract address | `null` |
| `.message(String)` | UTF-8 message | Empty |
| `.message(Bytes)` | Binary message | Empty |
| `.payload(TxPayload)` | BIP transaction payload | `null` |
| `.referenceHash(Hash)` | Reference to another transaction | `null` |

### Build & Sign Methods

| Method | Description |
|--------|-------------|
| `.sign(PrivateKey)` | Build and sign transaction |
| `.buildUnsigned()` | Build without signature |
| `.computeHash()` | Preview hash that will be signed |
| `.estimateSize()` | Estimate transaction size in bytes |

## Validation Rules

`TxBuilder` automatically validates transactions based on type:

### TRANSFER
- ✅ `recipient` required
- ✅ `nonce` required
- ✅ `amount` required
- ✅ `fee` required

### BIP_CREATE / BIP_VOTE
- ✅ `nonce` required
- ✅ `payload` required

## Error Handling

```java
try {
    Tx tx = TxBuilder.create()
        .type(TxType.TRANSFER)
        .network(Network.MAINNET)
        // Missing recipient!
        .nonce(1L)
        .sign(myKey);
} catch (CryptoJException e) {
    System.err.println("Error: " + e.getMessage());
    // "Recipient is required for TRANSFER transactions"
}
```

### Common Errors

| Error | Cause |
|-------|-------|
| `Transaction type is required` | Missing `.type()` |
| `Network is required` | Missing `.network()` |
| `Nonce is required` | Missing `.nonce()` |
| `Recipient is required for TRANSFER` | Transfer without recipient |
| `Payload is required for BIP_CREATE` | BIP transaction without payload |
| `Private key address does not match sender` | Signing with wrong key |

## Best Practices

### 1. Always Use Try-Catch

```java
try {
    Tx tx = TxBuilder.create()
        .type(TxType.TRANSFER)
        // ...
        .sign(myKey);
} catch (CryptoJException e) {
    // Handle error
}
```

### 2. Use Amounts Utility

```java
// ✅ Good - readable and blockchain agnostic
.amount(Amounts.tokens(100))
.fee(Amounts.Fees.STANDARD)

// ❌ Avoid - Ethereum terminology
.amount(Wei.fromEth(100))
```

### 3. Verify Signature After Signing

```java
Tx tx = TxBuilder.create()
    .type(TxType.TRANSFER)
    // ...
    .sign(myKey);

// Verify
Hash txHash = TxUtil.hashTxForSigning(tx);
boolean isValid = tx.getSignature().validate(txHash, tx.getSender());
assert(isValid);
```

### 4. Preview Before Signing

```java
TxBuilder builder = TxBuilder.create()
    .type(TxType.TRANSFER)
    // ...

// Show user what they're signing
Hash hash = builder.computeHash();
int size = builder.estimateSize();
System.out.println("You are signing hash: " + hash.toHexString());
System.out.println("Transaction size: " + size + " bytes");

// User confirms...
Tx tx = builder.sign(myKey);
```

## Examples

See `TxBuilderDemo.java` for complete working examples covering:
- Simple transfers
- Transfers with messages
- BIP token creation
- Token minting
- Custom timestamps
- Hash preview
- Unsigned transactions

## Running Tests

```bash
mvn test -Dtest=TxBuilderTest
```

## Integration with Wallet

### Basic Wallet Flow

```java
public class Wallet {
    private PrivateKey privateKey;
    private long nonce = 0;

    public Tx sendTokens(Address to, Wei amount) throws CryptoJException {
        Tx tx = TxBuilder.create()
            .type(TxType.TRANSFER)
            .network(Network.MAINNET)
            .recipient(to)
            .amount(amount)
            .fee(calculateFee(amount))
            .nonce(nonce++)
            .sign(privateKey);

        broadcast(tx);
        return tx;
    }

    private Wei calculateFee(Wei amount) {
        return Amounts.Fees.STANDARD;
    }

    private void broadcast(Tx tx) {
        // Broadcast to network
    }
}
```

## License

Part of GoldenEra CryptoJ library.

## Support

For issues or questions, please open an issue on GitHub.
