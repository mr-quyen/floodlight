package net.floodlightcontroller.test;

import com.google.common.util.concurrent.ListenableFuture;
import net.floodlightcontroller.core.*;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.Ethernet;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.*;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.*;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;

import static org.projectfloodlight.openflow.types.OFPort.*;

/**
 * Created by 1000884 on 10/28/16.
 */

public class Test_Quyen implements  IOFMessageListener, IFloodlightModule, IOFSwitchListener {

    private BlockingQueue<Map<DatapathId, List<OFStatsReply>>> replies;

    protected IFloodlightProviderService floodlightProvider;
    protected static org.slf4j.Logger logger;
    protected IOFSwitchService  switchService;
    protected OFFactory myFactory;
    private  ArrayList<DatapathId> dpids;
    private int queryInterval = 2;
    private  int MONITOR_COOKIE = 9;
    private  int ZOOMIN_COOKIE = 10;
    public boolean isZoomed  = false;

//    protected Queue<List<OFStatsReply>> replies;

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
//        return null;
        Ethernet eth =
                IFloodlightProviderService.bcStore.get(cntx,
                        IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

        Long sourceMACHash = eth.getSourceMACAddress().getLong();

        return Command.CONTINUE;
    }

    @Override
    public String getName() {
//        return null;
        return Test_Quyen.class.getSimpleName();
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return false;
    }


    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        return null;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        return null;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        return null;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        // Initiate all "" and methods
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
//        macAddresses = new ConcurrentSkipListSet<Long>();
        switchService        = context.getServiceImpl(IOFSwitchService.class);
        logger = LoggerFactory.getLogger(Test_Quyen.class);
        myFactory = OFFactories.getFactory(OFVersion.OF_13);
        replies = new LinkedBlockingDeque<Map<DatapathId, List<OFStatsReply>>>();
        dpids = new ArrayList<>();

    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
        switchService.addOFSwitchListener(this);


        //TODO: query thread

        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                writeToFile(String.valueOf(switchService.getAllSwitchDpids()));
                getSwitchStatistics(switchService.getAllSwitchDpids(), OFStatsType.FLOW);
            }
        }).start();
        */
        //TODO: update
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                updateFlowStatsThread();
//            }
//        }).start();





    }
    // These methods
    @Override
    public void switchAdded(DatapathId switchId) {



    }

    @Override
    public void switchRemoved(DatapathId switchId) {

    }

    @Override
    public void switchActivated(DatapathId switchId)  {
        addGroupTable(switchId);
        addGotoGroupRule(switchId);


    }

    @Override
    public void switchPortChanged(DatapathId switchId, OFPortDesc port, PortChangeType type) {

    }

    @Override
    public void switchChanged(DatapathId switchId) {

    }

    private U64  addICMPRule(DatapathId switchId){

        IOFSwitch mySwitch = switchService.getSwitch(switchId);
        Match myMatch = myFactory.buildMatch()
//                .setExact(MatchField.IN_PORT, OFPort.of(1))
                .setExact(MatchField.ETH_TYPE, EthType.IPv4)
//                .setMasked(MatchField.IPV4_SRC, IPv4AddressWithMask.of("10.0.0.1/32"))
                .setExact(MatchField.IP_PROTO, IpProtocol.ICMP)
//                .setExact(MatchField.ICMPV4_TYPE, ICMPv4Type.DESTINATION_UNREACHABLE)
//                .setExact(MatchField.TCP_DST, TransportPort.of(80))
                .build();

        OFInstructions instructions = myFactory.instructions();

        OFActions actions = myFactory.actions();
//        OFActionOutput actionOutput = (OFActionOutput) actions.buildOutput().setPort(OFPort.of(1));
        ArrayList<OFAction> actionList = new ArrayList<OFAction>();
        //TODO: findout what is the difference between actionList and instructionList
        //

        // OF instruction includes ApplyActions and gotoActions
        //
        /* Supply the OFAction list to the OFInstructionApplyActions. */
        OFInstructionApplyActions applyActions = instructions.buildApplyActions()
                .setActions(actionList)
                .build();

        ArrayList<OFInstruction> instructionList = new ArrayList<OFInstruction>();
        instructionList.add(applyActions);

        U64 cookie = U64.of(MONITOR_COOKIE);
        OFFlowAdd flowAdd = myFactory.buildFlowAdd()
                .setCookie(cookie)
                .setBufferId(OFBufferId.NO_BUFFER)
                .setHardTimeout(10000)
                .setIdleTimeout(10000)
                .setPriority(2)
                .setMatch(myMatch)
//                .setActions(actionList)
                .setInstructions(instructionList)
                // The first table match is table 0
                .setTableId(TableId.of(0))
                .build();

        mySwitch.write(flowAdd);
        System.out.println("debug: send dropping rules...");
        return cookie;
    }

    private  U64 addGotoTableRule(DatapathId switchId){
        IOFSwitch mySwitch = switchService.getSwitch(switchId);
        U64 cookie = U64.of(9000);

        IPv4Address mask = IPv4Address.of("255.0.0.0");
//        IPv4AddressWithMask ipAndMask = IPv4AddressWithMask.of(IPv4Address.of("192.168.0.1"), mask);

        Match myMatch = myFactory.buildMatch()
                .setExact(MatchField.ETH_TYPE, EthType.IPv4)
                .setMasked(MatchField.IPV4_SRC, IPv4Address.of("10.0.0.0"), mask)
                .build();

        OFInstruction gotoTable = mySwitch.getOFFactory().instructions()
                .gotoTable(TableId.of(1));
        List<OFInstruction> gotoLstTbl = new ArrayList<OFInstruction>();
        gotoLstTbl.add(gotoTable);

        OFFlowAdd defaultGotoTblAdd = myFactory.buildFlowAdd()
                .setCookie(cookie)
                .setBufferId(OFBufferId.NO_BUFFER)
                .setHardTimeout(10)
                .setIdleTimeout(10)
                .setPriority(4)
                .setMatch(myMatch)
//                .setActions(actionList)
//                .setInstructions(gotoLstTbl)
                // The first table match is table 0
                .setTableId(TableId.of(0))
                .build();

        mySwitch.write(defaultGotoTblAdd);
        System.out.println("debug: install goto rule ...");

        return cookie;
    }

    private  U64 addMonitorRule(DatapathId switchId){
        IOFSwitch mySwitch = switchService.getSwitch(switchId);
        U64 cookie = U64.of(MONITOR_COOKIE);

        IPv4Address mask = IPv4Address.of("255.0.0.0");
//        IPv4AddressWithMask ipAndMask = IPv4AddressWithMask.of(IPv4Address.of("192.168.0.1"), mask);

        Match myMatch = myFactory.buildMatch()
                .setExact(MatchField.ETH_TYPE, EthType.IPv4)
//                .setExact(MatchField.IP_PROTO, IpProtocol.ICMP)
                .setMasked(MatchField.IPV4_SRC, IPv4Address.of("160.0.0.0"), mask)
                .build();

        OFInstructions instructions = myFactory.instructions();

        OFActions actions = myFactory.actions();
//        OFActionOutput actionOutput = (OFActionOutput) actions.buildOutput().setPort(OFPort.of(1));
        ArrayList<OFAction> actionList = new ArrayList<OFAction>();
        //TODO: findout what is the difference between actionList and instructionList
        //

        // OF instruction includes ApplyActions and gotoActions
        //
        /* Supply the OFAction list to the OFInstructionApplyActions. */
        OFInstructionApplyActions applyActions = instructions.buildApplyActions()
                .setActions(actionList)
                .build();

        ArrayList<OFInstruction> instructionList = new ArrayList<OFInstruction>();
        instructionList.add(applyActions);

        OFFlowAdd monitorRule = myFactory.buildFlowAdd()
                .setCookie(cookie)
                .setBufferId(OFBufferId.NO_BUFFER)
                .setHardTimeout(30000)
                .setIdleTimeout(30000)
                .setPriority(3)
                .setMatch(myMatch)
//                .setActions(actionList)
                .setInstructions(instructionList)
                // The first table match is table 0
                .setTableId(TableId.of(0))
                .build();

        mySwitch.write(monitorRule);
        System.out.println("debug: install goto rule ...");

        return cookie;
    }
    private  U64 addZoominRule(DatapathId switchId,  String maskString, String ipDestString){
        IOFSwitch mySwitch = switchService.getSwitch(switchId);
        U64 cookie = U64.of(ZOOMIN_COOKIE);

        IPv4Address ipMask = IPv4Address.of("255." + maskString + ".0.0");
        IPv4Address ipDest = IPv4Address.of("160." + ipDestString + ".0.0");
        Match myMatch = myFactory.buildMatch()
                .setExact(MatchField.ETH_TYPE, EthType.IPv4)
//                .setExact(MatchField.IP_PROTO, IpProtocol.ICMP)
                .setMasked(MatchField.IPV4_SRC, ipDest , ipMask)
                .build();

        OFInstructions instructions = myFactory.instructions();

        OFActions actions = myFactory.actions();
//        OFActionOutput actionOutput = (OFActionOutput) actions.buildOutput().setPort(OFPort.of(1));
        ArrayList<OFAction> actionList = new ArrayList<OFAction>();
        //TODO: findout what is the difference between actionList and instructionList
        //

        // OF instruction includes ApplyActions and gotoActions
        //
        /* Supply the OFAction list to the OFInstructionApplyActions. */
        OFInstructionApplyActions applyActions = instructions.buildApplyActions()
                .setActions(actionList)
                .build();

        ArrayList<OFInstruction> instructionList = new ArrayList<OFInstruction>();
        instructionList.add(applyActions);

        OFFlowAdd zoominRule = myFactory.buildFlowAdd()
                .setCookie(cookie)
                .setBufferId(OFBufferId.NO_BUFFER)
                .setHardTimeout(30000)
                .setIdleTimeout(30000)
                .setPriority(4)
                .setMatch(myMatch)
//                .setActions(actionList)
                .setInstructions(instructionList)
                // The first table match is table 0
                .setTableId(TableId.of(1))
                .build();

        mySwitch.write(zoominRule);
        System.out.println("debug: install goto rule ...");

        return cookie;
    }


    private U64  addGotoGroupRule(DatapathId switchId){

        IOFSwitch mySwitch = switchService.getSwitch(switchId);
        Match myMatch = myFactory.buildMatch()
//                .setExact(MatchField.IN_PORT, OFPort.of(1))
                .setExact(MatchField.ETH_TYPE, EthType.IPv4)
//                .setMasked(MatchField.IPV4_SRC, IPv4AddressWithMask.of("10.0.0.1/32"))
                .setExact(MatchField.IP_PROTO, IpProtocol.ICMP)
//                .setExact(MatchField.ICMPV4_TYPE, ICMPv4Type.DESTINATION_UNREACHABLE)
//                .setExact(MatchField.TCP_DST, TransportPort.of(80))
                .build();

            List<OFInstruction> instructions = new ArrayList<OFInstruction>();
            OFAction gotoAction = myFactory.actions().buildGroup()
                                     .setGroup(OFGroup.of(1)).build();
            List<OFAction> actions = new ArrayList<OFAction>();
            actions.add(gotoAction);

            OFInstruction applyActions = mySwitch.getOFFactory().instructions()
                    .applyActions(actions);
            instructions.add(applyActions);


            U64 cookie = U64.of(MONITOR_COOKIE);
            OFFlowAdd flowAdd = myFactory.buildFlowAdd()
                .setCookie(cookie)
                .setBufferId(OFBufferId.NO_BUFFER)
                .setHardTimeout(60)
                .setIdleTimeout(60)
                .setPriority(100)
                .setMatch(myMatch)
                .setActions(actions)
//                .setInstructions(instructions)
                // The first table match is table 0

//                .setOutGroup(OFGroup.of(1))

                .setTableId(TableId.of(0))
                .build();

        mySwitch.write(flowAdd);
        return cookie;
    }

    private  void addGroupTable(DatapathId switchId){
        ArrayList<OFBucket> bucketList = new ArrayList<>();
        ArrayList<OFAction> actionListDrop = new ArrayList<>(); // No action, means drop.

        OFActionOutput actionFlood = myFactory.actions().buildOutput()
                .setPort(FLOOD)
                .build();
        ArrayList<OFAction> actions = new ArrayList<>();
        actions.add(actionFlood);

        OFBucket bucketFlood = myFactory.buildBucket()
                .setActions(actions)
                .setWatchGroup(OFGroup.ANY)
                .setWatchPort(ANY)
                .build();
        bucketList.add(bucketFlood);

        OFGroupAdd addGroup = myFactory.buildGroupAdd()
                .setGroup(OFGroup.of(1))
                .setGroupType(OFGroupType.SELECT)
                .setBuckets( bucketList)
//                .setXid(0x2)
                .build();

        IOFSwitch mySwitch = switchService.getSwitch(switchId);
        mySwitch.write(addGroup);  // add group table

    }
    private   void writeToFile(String s){
        PrintWriter pw = null;

        try {
            File file = new File("/Users/quyen/results.txt");
            FileWriter fw = new FileWriter(file, true);
            pw = new PrintWriter(fw);
            pw.println(s);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }
    private void addDefaultGotoTblRule(DatapathId dpid, int curTblId, int gotoTblId) {
        IOFSwitch sw = switchService.getSwitch(dpid);

        OFInstruction gotoTable = sw.getOFFactory().instructions()
                .gotoTable(TableId.of(gotoTblId));
        List<OFInstruction> gotoLstTbl = new ArrayList<OFInstruction>();
        gotoLstTbl.add(gotoTable);

        OFFlowAdd defaultGotoTblAdd = sw.getOFFactory().buildFlowAdd()
                .setPriority(10)
                .setTableId(TableId.of(curTblId))
                .setInstructions(gotoLstTbl)
                .build();
        sw.write(defaultGotoTblAdd);
    }

    private void addGroupTable(DatapathId dpid, int groupId) {
        IOFSwitch sw = switchService.getSwitch(dpid);


        OFActionDecNwTtl decTTL = sw.getOFFactory().actions().decNwTtl();
        OFActionOutput gotoController = sw.getOFFactory().actions().buildOutput()
                .setMaxLen(0xFFffFFff)
                .setPort(OFPort.CONTROLLER)
                .build();

        List<OFAction> actions1 = new ArrayList<OFAction>();
        actions1.add(decTTL);
        actions1.add(gotoController);


        List<OFAction> actions2 = new ArrayList<OFAction>();
        actions2.add(decTTL);

        OFBucket bucket1 = sw.getOFFactory().buildBucket()
                .setWatchGroup(OFGroup.ANY)
                .setWatchPort(OFPort.ANY)
                .setWeight(1)
                .setActions(actions1)
                .build();

        OFBucket bucket2 = sw.getOFFactory().buildBucket()
                .setWatchGroup(OFGroup.ANY)
                .setWatchPort(OFPort.ANY)
                .setWeight(2)
                .setActions(actions2)
                .build();

        List<OFBucket> buckets = new ArrayList<OFBucket>();
        buckets.add(bucket1);
        buckets.add(bucket2);



            OFGroupAdd groupAdd = sw.getOFFactory().buildGroupAdd()
                    .setGroup(OFGroup.of(groupId))
                    .setGroupType(OFGroupType.SELECT)
                    .setBuckets(buckets)
                    .build();


       sw.write(groupAdd);
    }


    private  HashMap<DatapathId, List<OFStatsReply> > sendFflowStatsRequest(DatapathId switchId){

        IOFSwitch mySwitch = switchService.getSwitch(switchId);
        //TODO: add flow statistics request. If not specify Cookie and TableId --> queries all?
        OFStatsRequest<?> req = myFactory.buildFlowStatsRequest()
                .setCookie(U64.of(MONITOR_COOKIE))
                .setTableId(TableId.of(0))
                .build();
        //TODO: send flow start request
        ListenableFuture<?> future;
        List<OFStatsReply> values = null;
        future = mySwitch.writeStatsRequest(req);

        try {
            values = (List<OFStatsReply>) future.get(2, TimeUnit.SECONDS);
//            this.replies.put(values);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        HashMap<DatapathId, List<OFStatsReply> > result = new HashMap<>();
        result.put(switchId,values);
        return result;
    }
    public void updateThread (){
        Map<DatapathId, List<OFStatsReply>>  event;
        while(true){
            try {
                boolean monitored = false;
                event = replies.take();
                writeToFile("-----------------------------------------------------------");

                for (DatapathId swithDPID: event.keySet()){
                    //
                    writeToFile(swithDPID.toString());
                    List<OFStatsReply> flowStatsReply = event.get(swithDPID);

                    for (OFStatsReply entry: flowStatsReply) {

                        OFFlowStatsReply statsReply = (OFFlowStatsReply) entry;
                        for (OFFlowStatsEntry e : statsReply.getEntries()) {
                            if (e.getCookie().getValue() == MONITOR_COOKIE){
                                writeToFile(String.valueOf(e.getPacketCount().getValue()));

                                if (e.getPacketCount().getValue() > 5000 && !monitored){
                                    //TODO: (1) not monitor (2) add gototable rule and (2) install zommin rules
                                    this.isZoomed = true;
                                    addGotoTableRule(swithDPID);
                                    addZoominRule(swithDPID, "240", "0" );
                                    addZoominRule(swithDPID, "240", "16" );
                                    addZoominRule(swithDPID, "240", "32" );
                                    addZoominRule(swithDPID, "240", "48" );
                                    addZoominRule(swithDPID, "240", "64" );
                                    addZoominRule(swithDPID, "240", "80" );
                                    addZoominRule(swithDPID, "240", "96" );
                                    addZoominRule(swithDPID, "240", "112" );
                                    addZoominRule(swithDPID, "240", "128" );
                                    addZoominRule(swithDPID, "240", "144" );
                                    addZoominRule(swithDPID, "240", "160" );
                                    addZoominRule(swithDPID, "240", "176" );
                                    addZoominRule(swithDPID, "240", "192" );
                                    addZoominRule(swithDPID, "240", "208" );
                                    addZoominRule(swithDPID, "240", "224" );
                                    addZoominRule(swithDPID, "240", "240" );
//                                        this.addZoominRule(swithDPID, "128");
//                                        this.addZoominRule(swithDPID, "128");
                                }
                            }

                        }
                    }
                }
            } catch (InterruptedException e) {
                System.out.println(" Queue is empty");
                e.printStackTrace();
                continue;
            }
        }
    }

    private class GetStatisticsThread extends Thread {
        private List<OFStatsReply> statsReply;
        private DatapathId switchId;
        private OFStatsType statType;

        public GetStatisticsThread(DatapathId switchId, OFStatsType statType) {
            this.switchId = switchId;
            this.statType = statType;
            this.statsReply = null;
        }

        public List<OFStatsReply> getStatisticsReply() {
            return statsReply;
        }

        public DatapathId getSwitchId() {
            return switchId;
        }

        @Override
        public void run() {
            statsReply = getSwitchStatistics(switchId, statType);
        }
    }

    private void getSwitchStatistics(Set<DatapathId> dpids, OFStatsType statsType) {
//    private Map<DatapathId, List<OFStatsReply>> getSwitchStatistics(Set<DatapathId> dpids, OFStatsType statsType) {
        HashMap<DatapathId, List<OFStatsReply>> model = new HashMap<DatapathId, List<OFStatsReply>>();

        List<GetStatisticsThread> activeThreads = new ArrayList<GetStatisticsThread>(dpids.size());
        List<GetStatisticsThread> pendingRemovalThreads = new ArrayList<GetStatisticsThread>();
        GetStatisticsThread t;
        for (DatapathId d : dpids) {
            t = new GetStatisticsThread(d, statsType);
            activeThreads.add(t);
            t.start();
        }


		/* Join all the threads after the timeout. Set a hard timeout
		 * of 12 seconds for the threads to finish. If the thread has not
		 * finished the switch has not replied yet and therefore we won't
		 * add the switch's stats to the reply.
		 */
        for (int iSleepCycles = 0; iSleepCycles < queryInterval; iSleepCycles++) {
            for (GetStatisticsThread curThread : activeThreads) {
                if (curThread.getState() == Thread.State.TERMINATED) {
                    model.put(curThread.getSwitchId(), curThread.getStatisticsReply());
                    pendingRemovalThreads.add(curThread);
                }
            }

			/* remove the threads that have completed the queries to the switches */
            for (GetStatisticsThread curThread : pendingRemovalThreads) {
                activeThreads.remove(curThread);
            }

			/* clear the list so we don't try to double remove them */
            pendingRemovalThreads.clear();

			/* if we are done finish early */
            if (activeThreads.isEmpty()) {
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Error");
//                log.error("Interrupted while waiting for statistics", e);
            }
        }
        replies.add(model);
//        return model;
    }

    protected List<OFStatsReply> getSwitchStatistics(DatapathId switchId, OFStatsType statsType) {
        IOFSwitch sw = switchService.getSwitch(switchId);
        ListenableFuture<?> future;
        List<OFStatsReply> values = null;
        Match match;

//            OFStatsRequest<?> req = null;

        match = sw.getOFFactory().buildMatch().build();
//                    req = sw.getOFFactory().buildFlowStatsRequest()
//                            .setMatch(match)
//                            .setOutPort(OFPort.ANY)
//                            .setTableId(TableId.ALL)
//                            .build();
        OFStatsRequest<?> req = myFactory.buildFlowStatsRequest()
//                    .setCookie(U64.of(9))
//                    .setTableId(TableId.of(0))
                .build();


        try {
            if (req != null) {
                future = sw.writeStatsRequest(req);

//                values = (List<OFStatsReply>) future.get(3, TimeUnit.SECONDS);
                values = (List<OFStatsReply>) future.get(2, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            System.out.println("Error...");
//            log.error("Failure retrieving statistics from switch {}. {}", sw, e);
        }
        return values;
    }

}
