package org.stipendj.core;

import org.stipendj.utils.ListenerRegistration;
import org.stipendj.utils.Threading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by Eric on 2/21/2016.
 */
public class MasternodeSync {
    private static final Logger log = LoggerFactory.getLogger(MasternodeSync.class);
    public static final int MASTERNODE_SYNC_INITIAL       =    0;
    public static final int MASTERNODE_SYNC_SPORKS        =    1;
    public static final int  MASTERNODE_SYNC_LIST         =     2;
    public static final int  MASTERNODE_SYNC_MNW          =     3;
    public static final int  MASTERNODE_SYNC_GOVERNANCE   =     4;
    public static final int  MASTERNODE_SYNC_GOVOBJ       =     10;
    public static final int  MASTERNODE_SYNC_GOVERNANCE_FIN   = 11;
    public static final int  MASTERNODE_SYNC_FAILED       =     998;
    public static final int  MASTERNODE_SYNC_FINISHED     =     999;

    public static final int  MASTERNODE_SYNC_TIMEOUT      =    30;

    public HashMap<Sha256Hash, Integer> mapSeenSyncMNB;
    public HashMap<Sha256Hash, Integer> mapSeenSyncMNW;
    public HashMap<Sha256Hash, Integer> mapSeenSyncBudget;

    long lastMasternodeList;
    long lastMasternodeWinner;
    long lastBudgetItem;
    long lastFailure;
    int nCountFailures;

    // sum of all counts
    int sumMasternodeList;
    int sumMasternodeWinner;
    int sumBudgetItemProp;
    int sumBudgetItemFin;
    // peers that reported counts
    int countMasternodeList;
    int countMasternodeWinner;
    int countBudgetItemProp;
    int countBudgetItemFin;

    // Count peers we've requested the list from
    int RequestedMasternodeAssets;
    int RequestedMasternodeAttempt;

    // Time when current masternode asset sync started
    long nAssetSyncStarted;

    StoredBlock currentBlock;

    //NetworkParameters params;
    AbstractBlockChain blockChain;
    Context context;

    public int masterNodeCountFromNetwork() { return countMasternodeList != 0 ? sumMasternodeList / countMasternodeList : 0; }

    void setBlockChain(AbstractBlockChain blockChain) { this.blockChain = blockChain; updateBlockTip(blockChain.chainHead);}

    public MasternodeSync(Context context)
    {
        this.context = context;

        this.mapSeenSyncBudget = new HashMap<Sha256Hash, Integer>();
        this.mapSeenSyncMNB = new HashMap<Sha256Hash, Integer>();
        this.mapSeenSyncMNW = new HashMap<Sha256Hash, Integer>();

        eventListeners = new CopyOnWriteArrayList<ListenerRegistration<MasternodeSyncListener>>();

        reset();
    }

    void reset()
    {
        lastMasternodeList = Utils.currentTimeSeconds();
        lastMasternodeWinner = Utils.currentTimeSeconds();
        lastBudgetItem = Utils.currentTimeSeconds();
        mapSeenSyncMNB.clear();
        mapSeenSyncMNW.clear();
        mapSeenSyncBudget.clear();
        lastFailure = 0;
        nCountFailures = 0;
        sumMasternodeList = 0;
        sumMasternodeWinner = 0;
        sumBudgetItemProp = 0;
        sumBudgetItemFin = 0;
        countMasternodeList = 0;
        countMasternodeWinner = 0;
        countBudgetItemProp = 0;
        countBudgetItemFin = 0;
        RequestedMasternodeAssets = MASTERNODE_SYNC_INITIAL;
        RequestedMasternodeAttempt = 0;
        nAssetSyncStarted = Utils.currentTimeSeconds();//GetTime();
    }

    void addedMasternodeList(Sha256Hash hash)
    {
        if(context.masternodeManager.mapSeenMasternodeBroadcast.containsKey(hash)) {
            Integer count = mapSeenSyncMNB.get(hash);
            if(count != null) {
                lastMasternodeList = Utils.currentTimeSeconds();
                mapSeenSyncMNB.put(hash, mapSeenSyncMNB.get(hash)+1);
            }
            else {
                mapSeenSyncMNB.put(hash, 1);
                lastMasternodeList = Utils.currentTimeSeconds();
            }
        } else {
            lastMasternodeList = Utils.currentTimeSeconds();
            mapSeenSyncMNB.put(hash, 1);
        }
    }

    boolean isSynced()
    {
        return RequestedMasternodeAssets == MASTERNODE_SYNC_FINISHED;
    }

    /*void addedMasternodeWinner(Sha256Hash hash)
    {
        if(parmas.masternodePayments.mapMasternodePayeeVotes.count(hash)) {
            if(mapSeenSyncMNW[hash] < MASTERNODE_SYNC_THRESHOLD) {
                lastMasternodeWinner = GetTime();
                mapSeenSyncMNW[hash]++;
            }
        } else {
            lastMasternodeWinner = GetTime();
            mapSeenSyncMNW.insert(make_pair(hash, 1));
        }
    }*/

    /*void CMasternodeSync::AddedBudgetItem(uint256 hash)
    {
        if(budget.mapSeenMasternodeBudgetProposals.count(hash) || budget.mapSeenMasternodeBudgetVotes.count(hash) ||
                budget.mapSeenFinalizedBudgets.count(hash) || budget.mapSeenFinalizedBudgetVotes.count(hash)) {
            if(mapSeenSyncBudget[hash] < MASTERNODE_SYNC_THRESHOLD) {
                lastBudgetItem = GetTime();
                mapSeenSyncBudget[hash]++;
            }
        } else {
            lastBudgetItem = GetTime();
            mapSeenSyncBudget.insert(make_pair(hash, 1));
        }
    }*/

    boolean isBudgetPropEmpty()
    {
        return sumBudgetItemProp==0 && countBudgetItemProp>0;
    }

    boolean isBudgetFinEmpty()
    {
        return sumBudgetItemFin==0 && countBudgetItemFin>0;
    }

    void getNextAsset()
    {
        switch(RequestedMasternodeAssets)
        {
            case(MASTERNODE_SYNC_INITIAL):
            case(MASTERNODE_SYNC_FAILED): // should never be used here actually, use Reset() instead
                clearFulfilledRequest();
                RequestedMasternodeAssets = MASTERNODE_SYNC_SPORKS;
                break;
            case(MASTERNODE_SYNC_SPORKS):
                lastMasternodeList = Utils.currentTimeSeconds();
                RequestedMasternodeAssets = MASTERNODE_SYNC_LIST;

                //If we are in lite mode and allowing InstantX, then only sync the sporks
                if(context.isLiteMode() && context.allowInstantXinLiteMode()) {
                    RequestedMasternodeAssets = MASTERNODE_SYNC_FINISHED;
                    queueOnSyncStatusChanged(RequestedMasternodeAssets, 1.0);
                }
                break;
            case(MASTERNODE_SYNC_LIST):
                lastMasternodeWinner = Utils.currentTimeSeconds();
           /*     RequestedMasternodeAssets = MASTERNODE_SYNC_MNW;  //TODO:  Reactivate when sync needs Winners and Budget
                break;
            case(MASTERNODE_SYNC_MNW):
                RequestedMasternodeAssets = MASTERNODE_SYNC_BUDGET
                lastBudgetItem = Utils.currentTimeSeconds();
                break;
            case(MASTERNODE_SYNC_BUDGET):*/
                log.info("CMasternodeSync::GetNextAsset - Sync has finished");
                RequestedMasternodeAssets = MASTERNODE_SYNC_FINISHED;
                queueOnSyncStatusChanged(RequestedMasternodeAssets, 1.0);
                break;
        }
        RequestedMasternodeAttempt = 0;
        nAssetSyncStarted = Utils.currentTimeSeconds();
        //queueOnSyncStatusChanged(RequestedMasternodeAssets);
    }

    public int getSyncStatusInt()
    { return RequestedMasternodeAssets; }

    public String getSyncStatus()
    {
        switch (RequestedMasternodeAssets) {
            case MASTERNODE_SYNC_INITIAL: return ("Synchronization pending...");
            case MASTERNODE_SYNC_SPORKS: return ("Synchronizing sporks...");
            case MASTERNODE_SYNC_LIST: return ("Synchronizing masternodes...");
            case MASTERNODE_SYNC_MNW: return ("Synchronizing masternode winners...");
            case MASTERNODE_SYNC_GOVERNANCE: return ("Synchronizing governance objects...");
            case MASTERNODE_SYNC_FAILED: return ("Synchronization failed");
            case MASTERNODE_SYNC_FINISHED: return ("Synchronization finished");
        }
        return "";
    }
    public String getAssetName()
    {
        switch(RequestedMasternodeAssets)
        {
            case(MASTERNODE_SYNC_INITIAL):
                return "MASTERNODE_SYNC_INITIAL";
            case(MASTERNODE_SYNC_FAILED): // should never be used here actually, use Reset() instead
                return "MASTERNODE_SYNC_FAILED";
            case(MASTERNODE_SYNC_SPORKS):
                return "MASTERNODE_SYNC_SPORKS";
            case(MASTERNODE_SYNC_LIST):
                return "MASTERNODE_SYNC_LIST";
            case(MASTERNODE_SYNC_MNW):
                return "MASTERNODE_SYNC_MNW";
            case(MASTERNODE_SYNC_GOVERNANCE):
                return "MASTERNODE_SYNC_GOVERNANCE";
        }
        return "Invalid asset name";
    }


    void processSyncStatusCount(Peer peer, SyncStatusCount ssc)
    {
            if(RequestedMasternodeAssets >= MASTERNODE_SYNC_FINISHED) return;

            //this means we will receive no further communication
            switch(ssc.itemId)
            {
                case(MASTERNODE_SYNC_LIST):
                    if(ssc.itemId != RequestedMasternodeAssets) return;
                    sumMasternodeList += ssc.count;
                    countMasternodeList++;
                    peer.setMasternodeListCount(ssc.count);
                    break;
                case(MASTERNODE_SYNC_MNW):
                    if(ssc.itemId != RequestedMasternodeAssets) return;
                    sumMasternodeWinner += ssc.count;
                    countMasternodeWinner++;
                    break;
                case(MASTERNODE_SYNC_GOVOBJ):
                    if(RequestedMasternodeAssets != MASTERNODE_SYNC_GOVERNANCE) return;
                    sumBudgetItemProp += ssc.count;
                    countBudgetItemProp++;
                    break;
                case(MASTERNODE_SYNC_GOVERNANCE_FIN):
                    if(RequestedMasternodeAssets != MASTERNODE_SYNC_GOVERNANCE) return;
                    sumBudgetItemFin += ssc.count;
                    countBudgetItemFin++;
                    break;
            }

            log.info("CMasternodeSync:ProcessMessage - ssc - got inventory count {} {}", ssc.itemId, ssc.count);
        //queueOnSyncStatusChanged(RequestedMasternodeAssets);

    }

    void clearFulfilledRequest()
    {
        //TODO:get the peergroup lock
        //TRY_LOCK(cs_vNodes, lockRecv);
        //if(!lockRecv) return;

        if(context.peerGroup == null)
            return;

        ReentrantLock nodeLock = context.peerGroup.getLock();

        if(!nodeLock.tryLock())
            return;

        try {
            for (Peer pnode : context.peerGroup.getConnectedPeers())
            //BOOST_FOREACH(CNode* pnode, vNodes)
            {
                pnode.clearFulfilledRequest("spork-sync");
                pnode.clearFulfilledRequest("masternode-winner-sync");
                pnode.clearFulfilledRequest("governance-sync");
                pnode.clearFulfilledRequest("masternode-sync");
            }
        } finally {
            nodeLock.unlock();
        }
    }

    static boolean fBlockchainSynced = false;
    static long lastProcess = Utils.currentTimeSeconds();



    public boolean isBlockchainSynced()
    {
        // if the last call to this function was more than 60 minutes ago (client was in sleep mode) reset the sync process
        if(Utils.currentTimeSeconds() - lastProcess > 60*60) {
            reset();
            fBlockchainSynced = false;
        }
        lastProcess = Utils.currentTimeSeconds();

        if(fBlockchainSynced) return true;

        //if (fImporting || fReindex) return false;


        if(currentBlock == null) return false;



        //if(pindex == NULL) return false;


        if(currentBlock.getHeader().getTimeSeconds() + 60*60 < Utils.currentTimeSeconds())
            return false;

        fBlockchainSynced = true;

        return true;
    }
    static int tick = 0;

    public void processTick()
    {

        if(tick++ %6 != 0) return;
        if(currentBlock == null) return;

        int mnCount = context.masternodeManager.countEnabled();

        if(isSynced()) {
        /*
            Resync if we lose all masternodes from sleep/wake or failure to sync originally
        */
            if(context.masternodeManager.countEnabled() == 0) {
                reset();
            } else
                //if syncing is complete and we have masternodes, return
                return;
        }

        //try syncing again
        if(RequestedMasternodeAssets == MASTERNODE_SYNC_FAILED && lastFailure + (1*60) < Utils.currentTimeSeconds()) {
            reset();
        } else if (RequestedMasternodeAssets == MASTERNODE_SYNC_FAILED) {
            return;
        }

        double nSyncProgress = (double)(RequestedMasternodeAttempt + (double)(RequestedMasternodeAssets - 1) * 8) / (8*4);
        log.info("CMasternodeSync::Process() - tick {} RequestedMasternodeAttempt {} RequestedMasternodeAssets {} nSyncProgress {}", tick, RequestedMasternodeAttempt, RequestedMasternodeAssets, nSyncProgress);
        queueOnSyncStatusChanged(RequestedMasternodeAssets, nSyncProgress);

        if(RequestedMasternodeAssets == MASTERNODE_SYNC_INITIAL)
            getNextAsset();

        // sporks synced but blockchain is not, wait until we're almost at a recent block to continue
        if(context.getParams().getId().equals(NetworkParameters.ID_REGTEST) &&
                !isBlockchainSynced() && RequestedMasternodeAssets > MASTERNODE_SYNC_SPORKS) return;

        //TRY_LOCK(cs_vNodes, lockRecv);
        //if(!lockRecv) return;

        if(context.peerGroup == null)
            return;

        ReentrantLock nodeLock = context.peerGroup.getLock();

        if(!nodeLock.tryLock())
            return;

        try {

            //BOOST_FOREACH(CNode* pnode, vNodes)
            for (Peer pnode : context.peerGroup.getConnectedPeers()) {
                // QUICK MODE (REGTEST ONLY!)
                if (context.getParams().getId().equals(NetworkParameters.ID_REGTEST)) {
                    if (RequestedMasternodeAttempt <= 2) {
                        pnode.sendMessage(new GetSporksMessage(context.getParams())); //get current network sporks
                    } else if (RequestedMasternodeAttempt < 4) {
                        context.masternodeManager.dsegUpdate(pnode);
                    } else if (RequestedMasternodeAttempt < 6) {
                        int nMnCount = context.masternodeManager.countEnabled();
                        pnode.sendMessage(new GetMasternodePaymentRequestSyncMessage(context.getParams(), nMnCount)); //sync payees
                        pnode.sendMessage(new GetMasternodeVoteSyncMessage(context.getParams(), Sha256Hash.ZERO_HASH)); //sync masternode votes
                    } else {
                        RequestedMasternodeAssets = MASTERNODE_SYNC_FINISHED;
                    }
                    RequestedMasternodeAttempt++;
                    return;
                }

                // NORMAL NETWORK MODE - TESTNET/MAINNET

                // SPORK : ALWAYS ASK FOR SPORKS AS WE SYNC (we skip this mode now)
                if (RequestedMasternodeAssets == MASTERNODE_SYNC_SPORKS) {
                    if (pnode.hasFulfilledRequest("spork-sync")) continue;
                    pnode.fulfilledRequest("spork-sync");

                    pnode.sendMessage(new GetSporksMessage(context.getParams())); //get current network sporks

                    if(RequestedMasternodeAssets == MASTERNODE_SYNC_SPORKS) {
                        getNextAsset();
                        return;
                    }
                }
                // MNLIST : SYNC MASTERNODE LIST FROM OTHER CONNECTED CLIENTS

                if (RequestedMasternodeAssets == MASTERNODE_SYNC_LIST) {


                    if (pnode.getPeerVersionMessage().clientVersion < context.masternodePayments.getMinMasternodePaymentsProto())
                        continue;

                    // shall we move onto the next asset?
                    if (mnCount > context.masternodeManager.getEstimatedMasternodes((int)(currentBlock.getHeight() * 0.9))) {
                        getNextAsset();
                        return;
                    }

                    if (lastMasternodeList < Utils.currentTimeSeconds() - MASTERNODE_SYNC_TIMEOUT) { //hasn't received a new item in the last five seconds, so we'll move to the
                        getNextAsset();
                        return;
                    }
                    // requesting is the last thing we do (incase we needed to move to the next asset and we've requested from each peer already)

                    if (pnode.hasFulfilledRequest("masternode-sync")) continue;
                    pnode.fulfilledRequest("masternode-sync");

                    //see if we've synced the masternode list
                    /* note: Is this activing up? It's probably related to int CMasternodeMan::GetEstimatedMasternodes(int nBlock) */

                    context.masternodeManager.dsegUpdate(pnode);
                    RequestedMasternodeAttempt++;

                    return;
                }

                // MNW : SYNC MASTERNODE WINNERS FROM OTHER CONNECTED CLIENTS

                if (RequestedMasternodeAssets == MASTERNODE_SYNC_MNW) {

                    if (pnode.getPeerVersionMessage().clientVersion < context.masternodePayments.getMinMasternodePaymentsProto())
                        continue;


                    // Shall we move onto the next asset?
                    // --
                    // This might take a lot longer than 2 minutes due to new blocks, but that's OK. It will eventually time out if needed
                    if(lastMasternodeWinner < Utils.currentTimeSeconds() - MASTERNODE_SYNC_TIMEOUT){ //hasn't received a new item in the last five seconds, so we'll move to the
                        getNextAsset();
                        return;
                    }

                    // if mnpayments already has enough blocks and votes, move to the next asset
                    if(context.masternodePayments.isEnoughData(mnCount)) {
                        getNextAsset();
                        return;
                    }

                    // requesting is the last thing we do (incase we needed to move to the next asset and we've requested from each peer already)

                    if(pnode.hasFulfilledRequest("masternode-winner-sync")) continue;
                    pnode.fulfilledRequest("masternode-winner-sync");

                    pnode.sendMessage(new GetMasternodePaymentRequestSyncMessage(context.getParams(), mnCount)); //sync payees
                    RequestedMasternodeAttempt++;


                    return;
                }


                // GOVOBJ : SYNC GOVERNANCE ITEMS FROM OUR PEERS
                if (RequestedMasternodeAssets == MASTERNODE_SYNC_GOVERNANCE) {

                    if (pnode.getPeerVersionMessage().clientVersion < context.masternodePayments.getMinMasternodePaymentsProto())
                        continue;


                    // shall we move onto the next asset
                    // if(countBudgetItemProp > 0 && countBudgetItemFin)
                    // {
                    //     if(governance.CountProposalInventoryItems() >= (sumBudgetItemProp / countBudgetItemProp)*0.9)
                    //     {
                    //         if(governance.CountFinalizedInventoryItems() >= (sumBudgetItemFin / countBudgetItemFin)*0.9)
                    //         {
                    //             GetNextAsset();
                    //             return;
                    //         }
                    //     }
                    // }

                    //we'll start rejecting votes if we accidentally get set as synced too soon, this allows plenty of time
                    if(lastBudgetItem < Utils.currentTimeSeconds() - MASTERNODE_SYNC_TIMEOUT){
                        getNextAsset();

                        //try to activate our masternode if possible
                        context.activeMasternode.manageStatus();
                        return;
                    }

                    // requesting is the last thing we do, incase we needed to move to the next asset and we've requested from each peer already

                    if(pnode.hasFulfilledRequest("governance-sync")) continue;
                    pnode.fulfilledRequest("governance-sync");

                    //uint256 n = uint256();
                    pnode.sendMessage(new GetMasternodeVoteSyncMessage(context.getParams(), Sha256Hash.ZERO_HASH)); //sync masternode votes
                    RequestedMasternodeAttempt++;

                    return; //this will cause each peer to get one request each six seconds for the various assets we need
                    }

                }

        } finally {
            nodeLock.unlock();
        }
    }

    /******************************************************************************************************************/

    //region Event listeners
    private transient CopyOnWriteArrayList<ListenerRegistration<MasternodeSyncListener>> eventListeners;
    /**
     * Adds an event listener object. Methods on this object are called when something interesting happens,
     * like receiving money. Runs the listener methods in the user thread.
     */
    public void addEventListener(MasternodeSyncListener listener) {
        addEventListener(listener, Threading.USER_THREAD);
}

    /**
     * Adds an event listener object. Methods on this object are called when something interesting happens,
     * like receiving money. The listener is executed by the given executor.
     */
    public void addEventListener(MasternodeSyncListener listener, Executor executor) {
        // This is thread safe, so we don't need to take the lock.
        eventListeners.add(new ListenerRegistration<MasternodeSyncListener>(listener, executor));
        //keychain.addEventListener(listener, executor);
    }

    /**
     * Removes the given event listener object. Returns true if the listener was removed, false if that listener
     * was never added.
     */
    public boolean removeEventListener(MasternodeSyncListener listener) {
        //keychain.removeEventListener(listener);
        return ListenerRegistration.removeFromList(listener, eventListeners);
    }

    private void queueOnSyncStatusChanged(final int newStatus, final double syncStatus) {
        //checkState(lock.isHeldByCurrentThread());
        for (final ListenerRegistration<MasternodeSyncListener> registration : eventListeners) {
            if (registration.executor == Threading.SAME_THREAD) {
                registration.listener.onSyncStatusChanged(newStatus, syncStatus);
            } else {
                registration.executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        registration.listener.onSyncStatusChanged(newStatus, syncStatus);
                    }
                });
            }
        }
    }
    public void updateBlockTip(StoredBlock tip) {
        currentBlock = tip;
    }
}
