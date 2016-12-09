//package net.floodlightcontroller.forwarding;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentSkipListSet;
//import net.floodlightcontroller.util.*;
//import net.floodlightcontroller.core.internal.IOFSwitchService;
//import org.projectfloodlight.openflow.protocol.*;
//import org.projectfloodlight.openflow.protocol.action.OFAction;
//import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
//import org.projectfloodlight.openflow.protocol.action.OFActionSetField;
//import org.projectfloodlight.openflow.protocol.action.OFActionSetQueue;
//import org.projectfloodlight.openflow.protocol.action.OFActions;
//import org.projectfloodlight.openflow.protocol.match.Match;
//import org.projectfloodlight.openflow.protocol.match.MatchField;
//import org.projectfloodlight.openflow.types.*;
//import org.projectfloodlight.openflow.protocol.OFPacketIn;
//import net.floodlightcontroller.core.FloodlightContext;
//import net.floodlightcontroller.core.IFloodlightProviderService;
//import net.floodlightcontroller.core.IOFMessageListener;
//import net.floodlightcontroller.core.IOFSwitch;
//import net.floodlightcontroller.core.module.FloodlightModuleContext;
//import net.floodlightcontroller.core.module.FloodlightModuleException;
//import net.floodlightcontroller.core.module.IFloodlightModule;
//import net.floodlightcontroller.core.module.IFloodlightService;
//import net.floodlightcontroller.packet.Ethernet;
//import net.floodlightcontroller.packet.ICMP;
//import net.floodlightcontroller.packet.IPv4;
//import net.floodlightcontroller.packet.IPv6;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class PacketIn_Ipv6 implements IFloodlightModule, IOFMessageListener {
//
//	protected IFloodlightProviderService floodlightProvider;
//	protected Set<Integer> ipAddresses;
//	protected static Logger logger;
//	private IOFSwitchService switchService;
//	private int count = 0;
//	@Override
//	public String getName() {
//		// TODO Auto-generated method stub
//		return PacketIn_Ipv6.class.getSimpleName();
//		//return null;
//	}
//
//	@Override
//	public boolean isCallbackOrderingPrereq(OFType type, String name) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public boolean isCallbackOrderingPostreq(OFType type, String name) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public Command receive(IOFSwitch sw, OFMessage msg,
//			FloodlightContext cntx) {
//		// TODO Auto-generated method stub
//		switch(msg.getType()){
//		case PACKET_IN:
//	        Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
//	        if (eth.getEtherType()==EthType.IPv6){
//				count++;
//                }
//			if (count==15){
//				OFPort inport= OFMessageUtils.getInPort((OFPacketIn)msg);
//
//				ipv6_Policy(inport);
//				count = 0;
//			}
//	        break;
//	    default:
//	    	break;
//		}
//
//		return Command.CONTINUE;
//	}
//
//	@Override
//	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
//		// TODO Auto-generated method stub
//	    Collection<Class<? extends IFloodlightService>> l =
//	            new ArrayList<Class<? extends IFloodlightService>>();
//	        l.add(IFloodlightProviderService.class);
//	        return l;
//		//return null;
//	}
//
//	@Override
//	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
//		// TODO Auto-generated method stub
//		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
//	    ipAddresses = new ConcurrentSkipListSet<Integer>();
//	    logger = LoggerFactory.getLogger(PacketIn_Ipv6.class);
//		switchService = context.getServiceImpl(IOFSwitchService.class);
//	}
//
//	@Override
//	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
//		// TODO Auto-generated method stub
//		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
//	}
//
//	private void ipv6_Policy(OFPort inport){
//
//        // Create SW object, match field
//		IOFSwitch mySwitch = switchService.getSwitch(DatapathId.of("00:00:00:0a:f7:8e:2b:98"));
//		OFFactory myFactory = mySwitch.getOFFactory();
//		OFFactory myFactory1 = mySwitch.getOFFactory();
//
//		Match myMatch = myFactory.buildMatch()
//				.setExact(MatchField.ETH_TYPE, EthType.of(0x86dd))
//				.setExact(MatchField.IP_PROTO,IpProtocol.IPv6_ICMP)
//				.build();
//
//		Integer i = Integer.valueOf(inport.toString());
//		for (int k=1; k<1000;k++){
//			System.out.println(i);
//		}
//		Match myMatch1 = myFactory1.buildMatch()
//				.setExact(MatchField.ETH_TYPE, EthType.of(0x86dd))
//				.setExact(MatchField.IP_PROTO,IpProtocol.IPv6_ICMP)
//				.setExact(MatchField.IN_PORT,OFPort.of(i))
//				.build();
//
//		//.setExact(MatchField.ICMPV6_CODE,U32.ofRaw(0))
//		//.setExact(MatchField.ICMPV6_TYPE,U32.ofRaw(134))
//		// Set action field
//		List<OFAction> al = new ArrayList<OFAction>();
//		List<OFAction> al1 = new ArrayList<OFAction>();
//		OFActions actions = myFactory.actions();
//		OFActionOutput output = actions.buildOutput().setMaxLen(0xFFffFFff).setPort(OFPort.of(6)).build();
//		al.add(output);
//		// Create OF Modification message
//		OFActionSetQueue setQueue = actions.buildSetQueue().setQueueId(1).build();
//		OFActionOutput output2 = actions.buildOutput().setMaxLen(0xFFffFFff).setPort(OFPort.of(6)).build();
//		al1.add(setQueue);
//		al1.add(output2);
//
//		OFFlowMod.Builder fmb;
//		fmb = mySwitch.getOFFactory().buildFlowAdd();
//		fmb.setMatch(myMatch);
//		fmb.setHardTimeout(1000000);
//		fmb.setIdleTimeout(1000000);
//		fmb.setPriority(6000);
//		fmb.setActions(al);
//
//
//		OFFlowMod.Builder fmb1;
//		fmb1 = mySwitch.getOFFactory().buildFlowAdd();
//		fmb1.setMatch(myMatch1);
//		fmb1.setHardTimeout(1000000);
//		fmb1.setIdleTimeout(1000000);
//		fmb1.setPriority(6000);
//		fmb1.setActions(al1);
//		// send the OFMod message to switch
//		//mySwitch.write(fmb.build());
//		//inport = 0;
//		mySwitch.write(fmb1.build());
//		for (int l =1;l<10;l++){
//			System.out.println("Ipv6 Policy!!");
//		}
//	}
//}