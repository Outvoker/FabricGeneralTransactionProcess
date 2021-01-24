package cn.fudan.lib.ledger;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.openssl.PEMWriter;
import org.hyperledger.fabric.protos.ledger.rwset.kvrwset.KvRwset;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.ChaincodeResponse.Status;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.InvalidProtocolBufferRuntimeException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.*;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hyperledger.fabric.sdk.BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE;
import static org.junit.Assert.*;

/**
 * Test end to end scenario
 */
public class Sdkdemo {

    private static final TestConfig testConfig = TestConfig.getConfig();

//    private static final ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName("chaincode").setVersion("1.0").setPath("chaincode").build();
    private static final ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName("mycc").setVersion("1.0").setPath("mycc").build();
    private static final String channelName = "mychannel";
//    private static final String adminOrg = "auditing";
    private static final String adminOrg = "Org1";
    static final String TEST_ADMIN_NAME = "admin";
    private HFClient client1;

    String testTxID = null;  // save the CC invoke TxID and use in queries

    private final TestConfigHelper configHelper = new TestConfigHelper();

    private Collection<SampleOrg> testSampleOrgs;


    Map<String, Properties> clientTLSProperties = new HashMap<>();
    List<Orderer> orderers = new ArrayList<>();
    private Map<String, List<Peer>> orgPeers = new HashMap();


    public void checkConfig() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, MalformedURLException {
        out("\n\n\nRUNNING: Sdkdemo.\n");
        configHelper.clearConfig();
        configHelper.customizeConfig();

        testSampleOrgs = testConfig.getIntegrationTestsSampleOrgs();
        //Set up hfca for each sample org

        for(SampleOrg sampleOrg : testSampleOrgs) {
            sampleOrg.setCAClient(HFCAClient.createNewInstance(sampleOrg.getCALocation(), sampleOrg.getCAProperties()));
        }
    }

    public void clearConfig() {
        try {
            configHelper.clearConfig();
        } catch (Exception e) {
        }
    }
    
    public void setup() {
    	 
        try {

            ////////////////////////////
            // Setup

            //Create instance of client.
            client1 = HFClient.createNewInstance();

            client1.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

            // client.setMemberServices(sdecFabricCA);

            ////////////////////////////
            //Set up USERS

            //Persistence is not part of SDK. Sample file store is for demonstration purposes only!
            //   MUST be replaced with more robust application implementation  (Database, LDAP)
            File sampleStoreFile = new File(System.getProperty("java.io.tmpdir") + "/HFCSampletest.properties");
            if (sampleStoreFile.exists()) { //For testing start fresh
                sampleStoreFile.delete();
            }

            final SampleStore sampleStore = new SampleStore(sampleStoreFile);
            //  sampleStoreFile.deleteOnExit();

            //SampleUser can be any implementation that implements org.hyperledger.fabric.sdk.User Interface

            ////////////////////////////
            // get users for all orgs

            for (SampleOrg sampleOrg : testSampleOrgs) {
                HFCAClient ca = sampleOrg.getCAClient();
                final String sampleOrgName = sampleOrg.getName();
                final String sampleOrgDomainName = sampleOrg.getDomainName();
                ca.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
                //out(sampleOrgDomainName);


                    SampleUser peerOrgAdmin = sampleStore.getMember(sampleOrgName + "Admin", sampleOrgName, sampleOrg.getMSPID(),
                        findFile_sk(Paths.get(testConfig.getTestChannelPath(), "crypto-config/peerOrganizations/",
                                sampleOrgDomainName, format("/users/Admin@%s/msp/keystore",sampleOrgDomainName )).toFile()),
                        Paths.get(testConfig.getTestChannelPath(), "crypto-config/peerOrganizations/", sampleOrgDomainName,
                                format("/users/Admin@%s/msp/signcerts/Admin@%s-cert.pem", sampleOrgDomainName, sampleOrgDomainName)).toFile());
                
                //out("sampleOrg:" + sampleOrg.getName());

                sampleOrg.setPeerAdmin(peerOrgAdmin); //A special user that can crate channels, join peers and install chain code
                                                        // and jump tall blockchains in a single leap! 
            }

           
            
//            String[] peerList1= {"peer0.Org1.example.com","peer1.Org1.example.com",
//		             "peer0.Org2.example.com","peer1.Org2.example.com"};
//            reconstructChannel("mychannel", peerList1);

            String[] peerList1= {"peer0.org1.example.com",
                    "peer1.org1.example.com",
                    "peer0.org2.example.com",
                    "peer1.org2.example.com",

            };
//            String[] peerList1= {"peer0.org1.example.com",
//                    "peer0.org2.example.com",
//                    "peer0.org3.example.com",
//                    "peer0.org4.example.com",
//                    "peer0.org5.example.com",
//                    "peer0.org6.example.com",
//                    "peer0.org7.example.com",
//                    "peer0.org8.example.com",
//            };
            Channel mychannel=reconstructChannel(channelName, peerList1);

//            String[] peerList1= {"peer0.auditing.example.com","peer1.auditing.example.com"//,"peer2.auditing.example.com","peer3.auditing.example.com"
//                    ,"peer0.fudan.example.com","peer1.fudan.example.com"//,"peer2.fudan.example.com","peer3.fudan.example.com"
//                    ,"peer0.instech.example.com","peer1.instech.example.com"//,"peer2.instech.example.com","peer3.instech.example.com"
//                    ,"peer0.hcrd.example.com","peer1.hcrd.example.com"//,"peer2.hcrd.example.com","peer3.hcrd.example.com"
//                    ,"peer0.dtl.example.com","peer1.dtl.example.com"//,"peer2.dtl.example.com","peer3.dtl.example.com"
//            };
//            reconstructChannel("mychannel", peerList1);


            out("That's all folks!");

        } catch (Exception e) {
            e.printStackTrace();

            fail(e.getMessage());
        }

    }
    
    private Channel reconstructChannel(String name, String[] peerList) throws Exception {

        SampleOrg sampleOrg = testConfig.getIntegrationTestsSampleOrg(adminOrg);
        client1.setUserContext(sampleOrg.getPeerAdmin());
        Channel newChannel = client1.newChannel(name);

        out("Reconstructing channel %s", name);
        for (String orderName : sampleOrg.getOrdererNames()) {
            Orderer anOrderer=client1.newOrderer(orderName, sampleOrg.getOrdererLocation(orderName),
                    testConfig.getOrdererProperties(orderName));
            newChannel.addOrderer(anOrderer);
            orderers.add(anOrderer);
        }

        for (String peerName : peerList) { 
        	String[] ps = peerName.split("\\.");
        	String str = "Org" + ps[1].substring(3,4);
        	sampleOrg= testConfig.getIntegrationTestsSampleOrg(str);
        	 client1.setUserContext(sampleOrg.getPeerAdmin());
            String peerLocation = sampleOrg.getPeerLocation(peerName);
            
            Peer peer = client1.newPeer(peerName, peerLocation, testConfig.getPeerProperties(peerName));

            if(!orgPeers.containsKey(sampleOrg.getName())){
                List<Peer> peers = new ArrayList();
                orgPeers.put(sampleOrg.getName(),peers);
            }
            orgPeers.get(sampleOrg.getName()).add(peer);

            //Query the actual peer for which channels it belongs to and check it belongs to this channel
            Set<String> channels = client1.queryChannels(peer);
    		if (!channels.contains(name)) {
    			throw new AssertionError(format("Peer %s does not appear to belong to channel %s", peerName, name));
    		}
    		newChannel.addPeer(peer);
//            newChannel.joinPeer(peer, createPeerOptions().setPeerRoles(EnumSet.of(Peer.PeerRole.ENDORSING_PEER, Peer.PeerRole.LEDGER_QUERY, Peer.PeerRole.CHAINCODE_QUERY, Peer.PeerRole.EVENT_SOURCE))); //Default is all roles.
//            sampleOrg.addPeer(peer);
        }
        client1.setUserContext(testConfig.getIntegrationTestsSampleOrg(adminOrg).getPeerAdmin());
        newChannel.initialize();
        out("Finished initialization channel %s", name);
        //Just see if we can get channelConfiguration. Not required for the rest of scenario but should work. 
        //final byte[] channelConfigurationBytes = newChannel.getChannelConfigurationBytes(); 
        //Configtx.Config channelConfig = Configtx.Config.parseFrom(channelConfigurationBytes); 
        //assertNotNull(channelConfig); 
        //Configtx.ConfigGroup channelGroup = channelConfig.getChannelGroup(); 
        //assertNotNull(channelGroup); 
        //Map<String, Configtx.ConfigGroup> groupsMap = channelGroup.getGroupsMap(); 
        //assertNotNull(groupsMap.get("Orderer")); 
        //assertNotNull(groupsMap.get("Application")); 

        //Before return lets see if we have the chaincode on the peers that we expect from End2endIT
        //And if they were instantiated too.

//        for (Peer peer : newChannel.getPeers()) {
//
//            if (!checkInstalledChaincode(client, peer, CHAIN_CODE_NAME, CHAIN_CODE_PATH, CHAIN_CODE_VERSION)) {
//                throw new AssertionError(format("Peer %s is missing chaincode name: %s, path:%s, version: %s",
//                        peer.getName(), CHAIN_CODE_NAME, CHAIN_CODE_PATH, CHAIN_CODE_PATH));
//            }
//
//            if (!checkInstantiatedChaincode(newChannel, peer, CHAIN_CODE_NAME, CHAIN_CODE_PATH, CHAIN_CODE_VERSION)) {
//
//                throw new AssertionError(format("Peer %s is missing instantiated chaincode name: %s, path:%s, version: %s",
//                        peer.getName(), CHAIN_CODE_NAME, CHAIN_CODE_PATH, CHAIN_CODE_PATH));
//            }
//
//        }

        return newChannel;
    }

    public String invoke(String channelName, String fcnName, String[] para){
        try {
        	client1.setUserContext(testConfig.getIntegrationTestsSampleOrg(adminOrg).getPeerAdmin());
        	Channel channel=client1.getChannel(channelName);
            Collection<ProposalResponse> successful = new LinkedList<>();
            Collection<ProposalResponse> failed= new LinkedList<>();
            // client.setUserContext(sampleOrg.getUser(TESTUSER_1_NAME));
            //       client.setUserContext(sampleOrg.getAdmin());
            
            ///////////////
            /// Send transaction proposal to all peers
            TransactionProposalRequest transactionProposalRequest = client1.newTransactionProposalRequest();
            transactionProposalRequest.setChaincodeID(chaincodeID);
            transactionProposalRequest.setFcn(fcnName);
            transactionProposalRequest.setProposalWaitTime(testConfig.getProposalWaitTime());
            transactionProposalRequest.setArgs(para);
            
            Map<String, byte[]> tm2 = new HashMap<>();
            tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
            tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
            tm2.put("result", ":)".getBytes(UTF_8));  /// This should be returned see chaincode.
            transactionProposalRequest.setTransientMap(tm2);
            
//            out("sending transactionProposal to all endorsers with arguments: ");
            //+ "( "+ para[0] +", "+ para[1] +", "+ para[2]+", "+ para[3]+", "+ para[4]+", "+ para[5]+", "+ para[6]+" )");
//            for(String value : para) {
//            	if (value!=null) {
//            		System.out.print(value+" ");
//            	}else {
//            		break;
//            	}
//            }
            //add random peers from demander and supplier into endorserSet
            Collection <Peer> endorserSet  = new LinkedList<>();
//            List<Peer> demPeers = orgPeers.get("Org"+para[3]);
//            List<Peer> supPeers = orgPeers.get("Org"+para[4]);
            List<Peer> demPeers = orgPeers.get("Org1");
            List<Peer> supPeers = orgPeers.get("Org2");
            for (int i = 0; i < demPeers.size(); i++){
                endorserSet.add(demPeers.get(i));
                endorserSet.add(supPeers.get(i));
            }



            Collection<ProposalResponse> transactionPropResp = channel.sendTransactionProposal(transactionProposalRequest, endorserSet);
            for (ProposalResponse response : transactionPropResp) {
                if (response.getStatus() == Status.SUCCESS) {
//                    System.out.println("================================success:" + response.getPeer().getName());
//                	out("Successful transaction proposal response Txid: %s from peer %s returned %s", response.getTransactionID(), response.getPeer().getName(), response.getProposalResponse().getResponse().getPayload().toStringUtf8());
                	successful.add(response);
//                	System.out.println(response.getChaincodeActionResponsePayload().toString());
                } else {
//                    System.out.println("================================failed:" + response.getPeer().getName());
                    failed.add(response);
                    out(response.getMessage());
                }
            }
            //Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils.getProposalConsistencySets(transactionPropResp); 
            //if (proposalConsistencySets.size() != 1) { 
            //    fail(format("Expected only one set of consistent proposal responses but got %d", proposalConsistencySets.size())); 
            //} 

//            out("Received %d transaction proposal responses. Successful+verified: %d . Failed: %d", transactionPropResp.size(), successful.size(), failed.size());
//            if (failed.size() > 0) {
//                ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
//                fail("Not enough endorsers for invoke:" + failed.size() + " endorser error: " +
//                        firstTransactionProposalResponse.getMessage() +
//                        ". Was verified: " + firstTransactionProposalResponse.isVerified());
//            }
//            out("Successfully received transaction proposal responses.");
            
            //ProposalResponse resp = transactionPropResp.iterator().next();
            //byte[] x = resp.getChainCodeActionResponsePayload();
            //String resultAsString = null;
            //if (x != null) {
            //     resultAsString = new String(x, "UTF-8");
            //}
            //assertEquals(":)", resultAsString);
            ////////////////////////////
            // Send Transaction Transaction to orderer
//            out("Sending chain code transaction to orderer.");
            //TransactionEvent txEvent=channel.sendTransaction(successful).get(testConfig.getTransactionWaitTime(), TimeUnit.SECONDS);
//            channel.sendTransaction(successful);
            Collection<Orderer> ordererSet = new ArrayList<>();
            ordererSet.add(orderers.get((int)(Math.random()*(orderers.size()-1))));
            channel.sendTransaction(successful,ordererSet);
            //assertTrue(txEvent.isValid()); // must be valid to be here.
            //testTxID = txEvent.getTransactionID();
            //out("Finished transaction with transaction id %s", txEvent.getTransactionID());
//            out("Finished transaction with transaction id");
            
        } catch (Exception e) {
            out("Caught an exception while invoking chaincode");
            e.printStackTrace();
            fail("Failed invoking chaincode with error : " + e.getMessage());
            
        }
        
        return null;
    }      

    public String query( String channelName, String fcnName, String[] para){
            ////////////////////////////
            // Send Query Proposal to all peers
            //
    	String payload ="{}";
    	try{
            out("Now query chain code");
            Channel channel=client1.getChannel(channelName);
            QueryByChaincodeRequest queryByChaincodeRequest = client1.newQueryProposalRequest();
            queryByChaincodeRequest.setArgs(para);
            queryByChaincodeRequest.setFcn(fcnName);
            queryByChaincodeRequest.setChaincodeID(chaincodeID);

            Map<String, byte[]> tm2 = new HashMap<>();
            tm2.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
            tm2.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
            queryByChaincodeRequest.setTransientMap(tm2);
            out("sending QueryProposal to all peers with arguments:" + "(" + para[0] +")");
//            for(Peer peer:channel.getPeers()) {
//            	out(peer.getName());
//            }
//             Peer peer = channel.getPeers().iterator().next();
//             Collection <Peer> peerSet = new LinkedList<>();
//             peerSet.add(peer);

            Collection <Peer> peerSet = channel.getPeers();


            Collection <Peer> endorserSet  = new LinkedList<>();

//            for(Peer peer : peerSet) {
//                if (peer.getName().contains(adminOrg)) {
//                    endorserSet.add(peer);
//                }
//            }
            for(Peer peer : peerSet) {
                    endorserSet.add(peer);

            }

//            Collection<ProposalResponse> queryProposals = channel.queryByChaincode(queryByChaincodeRequest, endorserSet);
 //           ProposalResponse proposalResponse=queryProposals.iterator().next();
            Collection<ProposalResponse> queryProposals = channel.queryByChaincode(queryByChaincodeRequest, endorserSet);
//            out("handleing queryProposals.");
            for (ProposalResponse proposalResponse : queryProposals) {
                if (!proposalResponse.isVerified() || proposalResponse.getStatus() != Status.SUCCESS) {
                    fail("Failed query proposal from peer " + proposalResponse.getPeer().getName() + " status: " + proposalResponse.getStatus() +
                            ". Messages: " + proposalResponse.getMessage()
                            + ". Was verified : " + proposalResponse.isVerified());
                } else {
                    String result = proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8();
                    out("Query payload from peer %s returned %s", proposalResponse.getPeer().getName(), result);

                    if(!result.equals("{}")) payload = result;

                    // int status=proposalResponse.getProposalResponse().getResponse().getStatus();
                    //System.out.println("status:"+status);
//                	byte[] payload1 = proposalResponse.getProposalResponse().getResponse().getPayload().toByteArray();
//                	for(int i=0;i<payload1.length;i++){
//                	  System.out.print((char)payload1[i]);
//                    }
                }
            }
        } catch (Exception e) {
            out("Caught exception while running query");
            e.printStackTrace();
            fail("Failed during chaincode query with error : " + e.getMessage());
        }

        return payload;
    }


    static String getPEMStringFromPrivateKey(PrivateKey privateKey) throws IOException {
        StringWriter pemStrWriter = new StringWriter();
        PEMWriter pemWriter = new PEMWriter(pemStrWriter);

        pemWriter.writeObject(privateKey);

        pemWriter.close();

        return pemStrWriter.toString();
    }
    static void out(String format, Object... args) {

        System.err.flush();
        System.out.flush();

        System.out.println(format(format, args));
        System.err.flush();
        System.out.flush();

    }

    File findFile_sk(File directory) {

        File[] matches = directory.listFiles((dir, name) -> name.endsWith("_sk"));

        if (matches.length != 1) {
            throw new RuntimeException(format("Expected in %s only 1 sk file but found %d", directory.getAbsoluteFile().getName(), matches.length));
        }

        return matches[0]; 

    }


    static String printableString(final String string) {
        int maxLogStringLength = 64;
        if (string == null || string.length() == 0) {
            return string;
        }

        String ret = string.replaceAll("[^\\p{Print}]", "?");

        ret = ret.substring(0, Math.min(ret.length(), maxLogStringLength)) + (ret.length() > maxLogStringLength ? "..." : "");

        return ret;

    }
    ArrayList<String> blockWalker() throws InvalidArgumentException, ProposalException, IOException {
        Channel channel=client1.getChannel(channelName);
        ArrayList<String> transactions = new ArrayList<>();
        try {
            BlockchainInfo channelInfo = channel.queryBlockchainInfo();

            int transactionCount = 0;
            int transactionUnvaildCount = 0;
            for (long current = 0; current < channelInfo.getHeight();  ++current) {
                BlockInfo returnedBlock = channel.queryBlockByNumber(current);
                final long blockNumber = returnedBlock.getBlockNumber();

                out("current block number %d has data hash: %s", blockNumber, Hex.encodeHexString(returnedBlock.getDataHash()));
                out("current block number %d has Block data: %s", blockNumber, returnedBlock.getBlock().getData());
//                out("current block number %d has previous hash id: %s", blockNumber, Hex.encodeHexString(returnedBlock.getPreviousHash()));
//                out("current block number %d has calculated block hash is %s", blockNumber, Hex.encodeHexString(SDKUtils.calculateBlockHash(client,
//                        blockNumber, returnedBlock.getPreviousHash(), returnedBlock.getDataHash())));

//                final int envelopeCount = returnedBlock.getEnvelopeCount();
//                out("current block number %d has %d envelope count:", blockNumber, returnedBlock.getEnvelopeCount());
                int i = 0;

                for (BlockInfo.EnvelopeInfo envelopeInfo : returnedBlock.getEnvelopeInfos()) {
                    ++i;

//                    out("  Transaction number %d has transaction id: %s", i, envelopeInfo.getTransactionID());
//                    final String channelId = envelopeInfo.getChannelId();
//                    assertTrue(channelName.equals(channelId));

//                    out("  Transaction number %d has channel id: %s", i, channelId);
//                    out("  Transaction number %d has epoch: %d", i, envelopeInfo.getEpoch());
//                    out("  Transaction number %d has transaction timestamp: %tB %<te,  %<tY  %<tT %<Tp", i, envelopeInfo.getTimestamp());

//                    out("  Transaction number %d has type id: %s", i, envelopeInfo.getType());
//                    out("  Transaction number %d has nonce : %s", i, Hex.encodeHexString(envelopeInfo.getNonce()));
//                    out("  Transaction number %d has submitter mspid: %s,  certificate: %s", i, envelopeInfo.getCreator().getMspid(), envelopeInfo.getCreator().getId());

                    if (envelopeInfo.getType() == TRANSACTION_ENVELOPE) {
//                        if(transactionCount % groupLength == 0){
//                            lastTransactionEveryGroup.add(new DateTime(envelopeInfo.getTimestamp()).toString("HH:mm:ss.SSS"));
//                        }
                        transactions.add(new DateTime(envelopeInfo.getTimestamp()).toString("HH:mm:ss.SSS"));
                        out("  Transaction number %d has transaction timestamp:%s", i, new DateTime(envelopeInfo.getTimestamp()).toString("HH:mm:ss.SSS"));
                        ++transactionCount;
                        BlockInfo.TransactionEnvelopeInfo transactionEnvelopeInfo = (BlockInfo.TransactionEnvelopeInfo) envelopeInfo;
                        if(! transactionEnvelopeInfo.isValid()){
                            transactionUnvaildCount++;
                            out("  Transaction number %d isValid %b", i, transactionEnvelopeInfo.isValid());
                        }
                    }

                }
            }
            out("Transactioncount:%d            Transactions are unvalid:%d", transactionCount,transactionUnvaildCount);
        } catch (InvalidProtocolBufferRuntimeException e) {
            throw e.getCause();
        }
        return transactions;
    }
}

