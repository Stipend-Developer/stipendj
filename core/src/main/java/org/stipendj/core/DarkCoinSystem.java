package org.stipendj.core;

import org.stipendj.store.BlockStore;
import org.darkcoinj.DarkSend;
import org.darkcoinj.InstantSend;

import java.util.ArrayList;

/**
 * Created by Hash Engineering on 2/8/2015.
 */
public class DarkCoinSystem {
    public InstantSend instantx;
    public MasterNodeSystem masternode;
    public DarkSend darkSend;
    public NetworkParameters params;
    PeerGroup peerGroup;
    public BlockChain blockChain;
    public BlockStore blockStore;

    public static boolean fMasterNode = false;

    static public String strMasterNodePrivKey = "";
    String strMasterNodeAddr = "";
    public static final boolean fLiteMode = false;
    int nInstantXDepth = 1;
    int nDarksendRounds = 2;
    int nAnonymizeDarkcoinAmount = 1000;
    int nLiquidityProvider = 0;
    /** Spork enforcement enabled time */
    long enforceMasternodePaymentsTime = 4085657524L;
    int nMasternodeMinProtocol = 0;
    boolean fSucessfullyLoaded = false;
    boolean fEnableDarksend = false;
/** All denominations used by darksend */
    ArrayList<Long> darkSendDenominations;

    public static final boolean fDebug = true;

    public DarkCoinSystem(NetworkParameters params, PeerGroup peerGroup, BlockChain blockChain, BlockStore blockStore)
    {
        this.params = params;
        this.peerGroup = peerGroup;
        this.blockChain = blockChain;
        this.blockStore = blockStore;

        //instantSend = new InstantSend(this);
        masternode = new MasterNodeSystem();
        darkSend = new DarkSend();
    }
}
