package org.stipendj.core;

import java.math.BigInteger;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Hash Engineering Solutions
 * Date: 5/3/14
 * To change this template use File | Settings | File Templates.
 */
public class CoinDefinition {


    public static final String coinName = "Stipend";
    public static final String coinTicker = "SPD";
    public static final String coinURIScheme = "stipend";
    public static final String cryptsyMarketId = "155";
    public static final String cryptsyMarketCurrency = "SPD";
    public static final String PATTERN_PRIVATE_KEY_START_UNCOMPRESSED = "[7]";
    public static final String PATTERN_PRIVATE_KEY_START_COMPRESSED = "[X]";

    public enum CoinPrecision {
        Coins,
        Millicoins,
    }
    public static final CoinPrecision coinPrecision = CoinPrecision.Coins;

    public static final String UNSPENT_API_URL = "https://chainz.cryptoid.info/stipend/api.dws?q=unspent";

    public enum UnspentAPIType {
        BitEasy,
        Blockr,
        Abe,
        Cryptoid,
    }

    public static final UnspentAPIType UnspentAPI = UnspentAPIType.Cryptoid;

    enum CoinHash {
        SHA256,
        scrypt,
        x11
    }

    public static final CoinHash coinPOWHash = CoinHash.x11;

    public static final boolean checkpointFileSupport = true;
    public static final int TARGET_TIMESPAN = (int)(45 * 60);  // 45 minutes per difficulty cycle, on average.
    public static final int TARGET_SPACING = (int)(1 * 60);  // 1 minute  per block.
    public static final int INTERVAL = TARGET_TIMESPAN / TARGET_SPACING;  //36 blocks

    public static final int getIntervalCheckpoints() {
            return 1440;
    }

    public static final int spendableCoinbaseDepth = 90; //chainparams.h: static const int COINBASE_MATURITY
    public static final long MAX_COINS = 20000000000l; //chainparams.h:  MAX_MONEY

    public static final long DEFAULT_TX_FEE = 10 * 100 * 1000000;   // main.h -> minRelayTxFee, 10 SPD per kb
    public static final long DEFAULT_MIN_TX_FEE = 1000000;   // absolute value 0.01 SPD
    public static final long DUST_LIMIT = 6 * 100 * 1000000; // transaction.h -> IsDust
    public static final long INSTANTX_FEE = 1000000; // 0.01 SPD
    public static final boolean feeCanBeRaised = false;

    //
    // Stipend v0.11
    //
    public static final int PROTOCOL_VERSION = 70914;          //version.h PROTOCOL_VERSION
    public static final int MIN_PROTOCOL_VERSION = 70914;        //version.h MIN_PROTO_VERSION

    public static final int BLOCK_CURRENTVERSION = 5;   //CBlock::CURRENT_VERSION
    public static final int MAX_BLOCK_SIZE = 2 * 1000 * 1000;

    public static final boolean supportsBloomFiltering = true; //Requires PROTOCOL_VERSION 70000 in the client

    public static final int Port    = 46978;       //protocol.h GetDefaultPort(testnet=false)
    public static final int TestPort = 59432;     //protocol.h GetDefaultPort(testnet=true)

    /** LibZerocoin starting block height */
    public static final long TESTNET_ZEROCOIN_STARTING_BLOCK_HEIGHT = Integer.MAX_VALUE;
    public static final long MAINNET_ZEROCOIN_STARTING_BLOCK_HEIGHT = Integer.MAX_VALUE;

    //
    //  Production
    //
    public static final int AddressHeader = 125;             //base58.h CBitcoinAddress::PUBKEY_ADDRESS
    public static final int p2shHeader = 21;             //base58.h CBitcoinAddress::SCRIPT_ADDRESS
    public static final int dumpedPrivateKeyHeader = 128;   //common to all coins
    public static final long PacketMagic = 0xa3b7ef9c;

    //Genesis Block Information from main.cpp: LoadBlockIndex
    static public long genesisBlockDifficultyTarget = 0x1e0ffff0;         //main.cpp: LoadBlockIndex
    static public long genesisBlockTime = 1547320090;                       //main.cpp: LoadBlockIndex
    static public long genesisBlockNonce = 411832;                         //main.cpp: LoadBlockIndex
    static public String genesisHash = "000007929d05212de4e111923059bd919be7939194b5ede235d03911a1f61b46";  //main.cpp: hashGenesisBlock
    static public String genesisMerkleRoot = "6d07032c7d4a599d0fb17dcedfbe99158a616b2b3b51108bee32d754f4becf1b";
    static public int genesisBlockValue = 250;                                                              //main.cpp: LoadBlockIndex
    //taken from the raw data of the block explorer
    static public String genesisTxInBytes = "04ffff001d01042253746970656e6420436f6465626173652055706772616465202d20312f32382f3139";
    public static final String genesisTxPubKey = "04c10e83b2703cccdeaf7dbd62dd5855ac7c10bd055814ce121ba32607d573b8810c02c0582aed05b4deb9c4b77b26d92428c61256cd42774babea0a073b2ed0c9";
    public static BigInteger proofOfWorkLimit = Utils.decodeCompactBits(0x1e0fffffL);  //chainparams.cpp bnProofOfWorkLimit

    //net.cpp strDNSSeed
    static public String[] dnsSeeds = new String[] {
      "80.211.218.202",
      "45.77.142.115",
      "193.161.85.100",
      "130.185.250.231",
      "217.144.172.5",
      "188.213.168.166",
      "46.101.87.132",
      "85.255.13.67",
      "91.205.172.209",
      "45.76.59.44",
      "73.135.159.101",
      "217.61.1.166",
      "62.75.166.52",
      "194.182.81.9",
      "47.88.156.51",
      "85.25.13.223",
    };
    public static int minBroadcastConnections = 3;   //0 for default; we need more peers.

    //
    // TestNet - SPD
    //
    public static final boolean supportsTestNet = true;
    public static final int testnetAddressHeader = 139;             //base58.h CBitcoinAddress::PUBKEY_ADDRESS_TEST
    public static final int testnetp2shHeader = 19;             //base58.h CBitcoinAddress::SCRIPT_ADDRESS_TEST
    public static final long testnetPacketMagic = 0x467766BB;      // chainparams.cpp
    public static final String testnetGenesisHash =  "6cd37a546cfaafeee652fd0f3a85ba64c0f539f771a27fca9610cdc2f3278932";
    public static final long testnetGenesisBlockDifficultyTarget = 0x1E0FFFF0;         //chainparams.cpp: LoadBlockIndex
    public static final long testnetGenesisBlockTime = 1520769358L;                       //chainparams.cpp: LoadBlockIndex
    public static final long testnetGenesisBlockNonce = 2452017L;                         //chainparams.cpp: LoadBlockIndex

    //main.cpp GetBlockValue(height, fee)
    public static final Coin GetBlockReward(int height)
    {
        int COIN = 1;
        Coin nSubsidy = Coin.valueOf(100, 0);
        if (height == 1)
            nSubsidy = Coin.valueOf(420000, 0);
        return nSubsidy;
    }

    public static int subsidyDecreaseBlockCount = 210000;     //main.cpp GetBlockValue(height, fee)

    public static BigInteger testnetProofOfWorkLimit = Utils.decodeCompactBits(0x207fffffL);  //chainparams.cpp bnProofOfWorkLimit

    static public String[] testnetDnsSeeds = new String[] {
            "stipend.home"
    };

    //from main.h: CAlert::CheckSignature
    public static final String SATOSHI_KEY = "0000098d3ba6ba6e7423fa5cbd6a89e0a9a5348f88d332b44a5cb1a8b7ed2c1eaa335fc8dc4f012cb8241cc0bdafd6ca70c5f5448916e4e6f511bcd746ed57dc50";
    public static final String TESTNET_SATOSHI_KEY = "000010e83b2703ccf322f7dbd62dd5855ac7c10bd055814ce121ba32607d573b8810c02c0582aed05b4deb9c4b77b26d92428c61256cd42774babea0a073b2ed0c9";

    /** The string returned by getId() for the main, production network where people trade things. */
    public static final String ID_MAINNET = "org.stipend.production";
    /** The string returned by getId() for the testnet. */
    public static final String ID_TESTNET = "org.stipend.test";
    /** Unit test network. */
    public static final String ID_UNITTESTNET = "com.google.pivx.unittest";

    //checkpoints.cpp Checkpoints::mapCheckpoints
    public static void initCheckpoints(Map<Integer, Sha256Hash> checkpoints)
    {

//        checkpoints.put(  1500, Sha256Hash.wrap("000000aaf0300f59f49bc3e970bad15c11f961fe2347accffff19d96ec9778e3"));
//        checkpoints.put(  4991, Sha256Hash.wrap("000000003b01809551952460744d5dbb8fcbd6cbae3c220267bf7fa43f837367"));
//        checkpoints.put(  9918, Sha256Hash.wrap("00000000213e229f332c0ffbe34defdaa9e74de87f2d8d1f01af8d121c3c170b"));
//        checkpoints.put( 16912, Sha256Hash.wrap("00000000075c0d10371d55a60634da70f197548dbbfa4123e12abfcbc5738af9"));
//        checkpoints.put( 23912, Sha256Hash.wrap("0000000000335eac6703f3b1732ec8b2f89c3ba3a7889e5767b090556bb9a276"));
//        checkpoints.put( 35457, Sha256Hash.wrap("0000000000b0ae211be59b048df14820475ad0dd53b9ff83b010f71a77342d9f"));
//        checkpoints.put( 45479, Sha256Hash.wrap("000000000063d411655d590590e16960f15ceea4257122ac430c6fbe39fbf02d"));
//        checkpoints.put( 55895, Sha256Hash.wrap("0000000000ae4c53a43639a4ca027282f69da9c67ba951768a20415b6439a2d7"));
//        checkpoints.put( 68899, Sha256Hash.wrap("0000000000194ab4d3d9eeb1f2f792f21bb39ff767cb547fe977640f969d77b7"));
//        checkpoints.put( 74619, Sha256Hash.wrap("000000000011d28f38f05d01650a502cc3f4d0e793fbc26e2a2ca71f07dc3842"));
//        checkpoints.put( 75095, Sha256Hash.wrap("0000000000193d12f6ad352a9996ee58ef8bdc4946818a5fec5ce99c11b87f0d"));
//        checkpoints.put( 88805, Sha256Hash.wrap("00000000001392f1652e9bf45cd8bc79dc60fe935277cd11538565b4a94fa85f"));
//        checkpoints.put( 90544, Sha256Hash.wrap("000000000001b284b79a44a95215d7e6cf9e22cd4f9b562f2cc796e941e0e411"));
    }

    //Unit Test Information
    public static final String UNITTEST_ADDRESS = "XgxQxd6B8iYgEEryemnJrpvoWZ3149MCkK";
    public static final String UNITTEST_ADDRESS_PRIVATE_KEY = "XDtvHyDHk4S3WJvwjxSANCpZiLLkKzoDnjrcRhca2iLQRtGEz1JZ";

}
