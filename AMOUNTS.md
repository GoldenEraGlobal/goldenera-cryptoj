# Amounts - Wei Helper Utility

## 🎯 Purpose

`Amounts` is a utility class for working with Wei values in the GoldenEra blockchain **without Ethereum-specific terminology**.

## 📦 Basic Usage

```java
import global.goldenera.cryptoj.utils.Amounts;

// Whole tokens
Wei amount = Amounts.tokens(100);      // 100 tokens
Wei large = Amounts.tokens(1000000);   // 1 million tokens

// Tokens with decimal places
Wei precise = Amounts.tokensDecimal("1.5");    // 1.5 tokens
Wei fee = Amounts.tokensDecimal("0.001");      // 0.001 tokens

// Direct Wei values
Wei tiny = Amounts.wei(1000000000000000L);     // Exact Wei value

// Zero
Wei zero = Amounts.zero();
```

## 💰 Pre-defined Fee Constants

```java
import global.goldenera.cryptoj.utils.Amounts.Fees;

Wei standard = Amounts.Fees.STANDARD;  // 0.001 tokens
Wei low = Amounts.Fees.LOW;            // 0.0001 tokens  
Wei high = Amounts.Fees.HIGH;          // 0.01 tokens
Wei bip = Amounts.Fees.BIP;            // 0.01 tokens (for BIP transactions)
```

## 🔢 Conversion

1 **token** = 10^18 **wei** (same as ETH, but without Ethereum reference)

```java
Amounts.tokens(1)              // = 1_000_000_000_000_000_000 wei
Amounts.tokensDecimal("0.5")   // =   500_000_000_000_000_000 wei
Amounts.tokensDecimal("0.001") // =     1_000_000_000_000_000 wei
```

## ✨ Examples with TxBuilder

### Simple Transfer
```java
Tx tx = TxBuilder.create()
    .type(TxType.TRANSFER)
    .network(Network.MAINNET)
    .sender(myAddress)
    .recipient(recipientAddress)
    .amount(Amounts.tokensDecimal("1.5"))  // ⭐ Instead of Wei.fromEth()
    .fee(Amounts.Fees.STANDARD)            // ⭐ Pre-defined fee
    .nonce(1L)
    .sign(myKey);
```

### Token Mint
```java
Tx tx = TxBuilder.create()
    .tokenMint()
        .token(tokenAddress)
        .recipient(recipientAddress)
        .amount(Amounts.tokens(1000))      // ⭐ Whole tokens
        .minerFee(Amounts.Fees.STANDARD)
    .done()
    .network(Network.MAINNET)
    .sender(myAddress)
    .nonce(2L)
    .fee(Amounts.tokensDecimal("0.005"))   // ⭐ Precise fee
    .sign(myKey);
```

### Network Parameters
```java
Tx tx = TxBuilder.create()
    .setNetworkParams()
        .blockReward(Amounts.tokens(5))    // ⭐ Block reward
        .targetMiningTime(10000L)
    .done()
    .network(Network.MAINNET)
    .sender(authorityAddress)
    .nonce(1L)
    .sign(myKey);
```

## 🆚 BEFORE vs AFTER

### ❌ BEFORE (Ethereum terminology):
```java
.amount(Wei.fromEth(1))                                    // Misleading!
.fee(Wei.valueOf(BigInteger.valueOf(1000000000000000L)))  // Unreadable!
```

### ✅ AFTER (GoldenEra friendly):
```java
.amount(Amounts.tokens(1))           // Clear!
.fee(Amounts.tokensDecimal("0.001")) // Readable!
.fee(Amounts.Fees.STANDARD)          // Elegant!
```

## 🎓 API Reference

| Method | Parameter | Description | Example |
|--------|----------|-------------|---------|
| `tokens(long)` | Whole tokens | For large round amounts | `Amounts.tokens(1000)` |
| `tokens(BigInteger)` | Whole tokens | For very large amounts | `Amounts.tokens(BigInteger.valueOf(1000000))` |
| `tokensDecimal(String)` | Decimal string | For precise amounts with decimals | `Amounts.tokensDecimal("1.5")` |
| `wei(long)` | Wei units | For direct wei manipulation | `Amounts.wei(1000000000000000L)` |
| `wei(BigInteger)` | Wei units | For very precise values | `Amounts.wei(BigInteger...)` |
| `zero()` | - | Zero value | `Amounts.zero()` |

## 💡 Best Practices

1. **For typical amounts** - use `tokensDecimal()`:
   ```java
   .amount(Amounts.tokensDecimal("1.5"))
   .fee(Amounts.tokensDecimal("0.001"))
   ```

2. **For whole tokens** - use `tokens()`:
   ```java
   .amount(Amounts.tokens(100))
   .blockReward(Amounts.tokens(5))
   ```

3. **For fees** - use constants:
   ```java
   .fee(Amounts.Fees.STANDARD)  // Most common use case
   .fee(Amounts.Fees.BIP)       // For BIP transactions
   ```

4. **For precise Wei values** - use `wei()`:
   ```java
   .minerFee(Amounts.wei(1500000000000000L))
   ```

## 🔧 Implementation Details

```java
@UtilityClass
public class Amounts {
    public static final BigInteger WEI_PER_TOKEN = new BigInteger("1000000000000000000");
    
    public static Wei tokens(long tokens) {
        return Wei.valueOf(BigInteger.valueOf(tokens).multiply(WEI_PER_TOKEN));
    }
    
    public static Wei tokensDecimal(String tokensDecimal) {
        // Converts "1.5" to 1_500_000_000_000_000_000 wei
    }
    
    public static class Fees {
        public static final Wei STANDARD = wei(1000000000000000L);  // 0.001
        public static final Wei LOW = wei(100000000000000L);        // 0.0001
        public static final Wei HIGH = wei(10000000000000000L);     // 0.01
        public static final Wei BIP = HIGH;                         // 0.01
    }
}
```

## 📊 Comparison Table

| Scenario | Old Way (Ethereum) | New Way (GoldenEra) | Improvement |
|----------|-------------------|---------------------|-------------|
| 1 token | `Wei.fromEth(1)` | `Amounts.tokens(1)` | ✅ Clearer |
| 1.5 tokens | `Wei.valueOf(BigInteger.valueOf(1500...))` | `Amounts.tokensDecimal("1.5")` | ✅ Much simpler |
| Standard fee | `Wei.valueOf(BigInteger.valueOf(1000...))` | `Amounts.Fees.STANDARD` | ✅ Self-documenting |
| Custom fee | `Wei.fromEth(0.005)` | `Amounts.tokensDecimal("0.005")` | ✅ Blockchain agnostic |

## 🚀 Getting Started

```java
import global.goldenera.cryptoj.utils.Amounts;
import global.goldenera.cryptoj.utils.Amounts.Fees;

// Simple usage
Wei amount = Amounts.tokens(100);
Wei fee = Amounts.Fees.STANDARD;

// Advanced usage
Wei precise = Amounts.tokensDecimal("123.456789");
Wei custom = Amounts.wei(1234567890000000000L);
```

## 🎯 Why Amounts Over Wei.fromEth()?

1. **Blockchain Agnostic** - No Ethereum references
2. **More Readable** - `tokens()` vs `fromEth()`
3. **Type Safe** - Compile-time validation
4. **Convenient** - Pre-defined fee constants
5. **Flexible** - Multiple input formats (long, BigInteger, String)

---

**Made with ❤️ for GoldenEra Blockchain - Blockchain Agnostic!**
