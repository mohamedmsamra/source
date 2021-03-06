package org.seamcat.function;

import org.junit.Before;
import org.junit.Test;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.model.functions.FunctionException;
import org.seamcat.model.functions.Point2D;


public class DiscreteFunctionTest {

	private TestUtil testUtil;

	@Before
	public void setup() {
		testUtil = new TestUtil( 0.000001 );
	}

	@Test
	public void testSimple() throws FunctionException {
		DiscreteFunction simpleFunction = new DiscreteFunction();

		for ( int i=0; i<10; i++) {
			simpleFunction.addPoint( new Point2D( i, i));
		}

		testUtil.assertDoubleEquals(5.0, simpleFunction.evaluate( 5 ));
		testUtil.assertDoubleEquals(4.5, simpleFunction.evaluate( 4.5 ));
		testUtil.assertDoubleEquals(4.2, simpleFunction.evaluate( 4.2 ));
		testUtil.assertDoubleEquals(7.7, simpleFunction.evaluate( 7.7 ));
	}



	@Test
	public void testBig() throws FunctionException {
		DiscreteFunction big = createBig();
		
		testUtil.assertDoubleEquals(-4.016094, big.evaluate(37.6));
		testUtil.assertDoubleEquals(-18.5161695, big.evaluate(-166));
		testUtil.assertDoubleEquals(-18.384777405, big.evaluate(-160.1));
		testUtil.assertDoubleEquals(-18.38706277, big.evaluate(-160.2));
		testUtil.assertDoubleEquals(-18.389348135, big.evaluate(-160.3));
		testUtil.assertDoubleEquals(-18.3916335, big.evaluate(-160.4));
		testUtil.assertDoubleEquals(-18.393918865, big.evaluate(-160.5));
		testUtil.assertDoubleEquals(-18.396204230, big.evaluate(-160.6));
		testUtil.assertDoubleEquals(-18.398489595, big.evaluate(-160.7));
		testUtil.assertDoubleEquals(-18.40077496, big.evaluate(-160.8));
		testUtil.assertDoubleEquals(-18.403060325, big.evaluate(-160.9));
	}

	private DiscreteFunction createBig() {
		DiscreteFunction big = new DiscreteFunction();
		big.addPoint(new Point2D(-180, -18.79863133));
		big.addPoint(new Point2D(-179, -18.77970763));
		big.addPoint(new Point2D(-178, -18.76060161));
		big.addPoint(new Point2D(-177, -18.74131082));
		big.addPoint(new Point2D(-176, -18.72183276));
		big.addPoint(new Point2D(-175, -18.70216488));
		big.addPoint(new Point2D(-174, -18.6823046));
		big.addPoint(new Point2D(-173, -18.66224929));
		big.addPoint(new Point2D(-172, -18.64199626));
		big.addPoint(new Point2D(-171, -18.62154278));
		big.addPoint(new Point2D(-170, -18.60088607));
		big.addPoint(new Point2D(-169, -18.5800233));
		big.addPoint(new Point2D(-168, -18.55895159));
		big.addPoint(new Point2D(-167, -18.53766801));
		big.addPoint(new Point2D(-166, -18.51616956));
		big.addPoint(new Point2D(-165, -18.49445319));
		big.addPoint(new Point2D(-164, -18.47251582));
		big.addPoint(new Point2D(-163, -18.45035426));
		big.addPoint(new Point2D(-162, -18.42796531));
		big.addPoint(new Point2D(-161, -18.40534569));
		big.addPoint(new Point2D(-160, -18.38249204));
		big.addPoint(new Point2D(-159, -18.35940096));
		big.addPoint(new Point2D(-158, -18.33606897));
		big.addPoint(new Point2D(-157, -18.31249254));
		big.addPoint(new Point2D(-156, -18.28866804));
		big.addPoint(new Point2D(-155, -18.2645918));
		big.addPoint(new Point2D(-154, -18.24026007));
		big.addPoint(new Point2D(-153, -18.21566901));
		big.addPoint(new Point2D(-152, -18.19081472));
		big.addPoint(new Point2D(-151, -18.16569321));
		big.addPoint(new Point2D(-150, -18.14030042));
		big.addPoint(new Point2D(-149, -18.1146322));
		big.addPoint(new Point2D(-148, -18.08868432));
		big.addPoint(new Point2D(-147, -18.06245247));
		big.addPoint(new Point2D(-146, -18.03593222));
		big.addPoint(new Point2D(-145, -18.0091191));
		big.addPoint(new Point2D(-144, -17.98200849));
		big.addPoint(new Point2D(-143, -17.95459571));
		big.addPoint(new Point2D(-142, -17.92687597));
		big.addPoint(new Point2D(-141, -17.89884439));
		big.addPoint(new Point2D(-140, -17.87049595));
		big.addPoint(new Point2D(-139, -17.84182557));
		big.addPoint(new Point2D(-138, -17.81282802));
		big.addPoint(new Point2D(-137, -17.78349797));
		big.addPoint(new Point2D(-136, -17.75382999));
		big.addPoint(new Point2D(-135, -17.7238185));
		big.addPoint(new Point2D(-134, -17.69345781));
		big.addPoint(new Point2D(-133, -17.66274211));
		big.addPoint(new Point2D(-132, -17.63166545));
		big.addPoint(new Point2D(-131, -17.60022174));
		big.addPoint(new Point2D(-130, -17.56840475));
		big.addPoint(new Point2D(-129, -17.53620813));
		big.addPoint(new Point2D(-128, -17.50362536));
		big.addPoint(new Point2D(-127, -17.47064977));
		big.addPoint(new Point2D(-126, -17.43727453));
		big.addPoint(new Point2D(-125, -17.40349267));
		big.addPoint(new Point2D(-124, -17.36929702));
		big.addPoint(new Point2D(-123, -17.33468027));
		big.addPoint(new Point2D(-122, -17.29963491));
		big.addPoint(new Point2D(-121, -17.26415326));
		big.addPoint(new Point2D(-120, -17.22822744));
		big.addPoint(new Point2D(-119, -17.19184938));
		big.addPoint(new Point2D(-118, -17.15501081));
		big.addPoint(new Point2D(-117, -17.11770326));
		big.addPoint(new Point2D(-116, -17.07991802));
		big.addPoint(new Point2D(-115, -17.04164618));
		big.addPoint(new Point2D(-114, -17.00287859));
		big.addPoint(new Point2D(-113, -16.96360587));
		big.addPoint(new Point2D(-112, -16.92381838));
		big.addPoint(new Point2D(-111, -16.88350624));
		big.addPoint(new Point2D(-110, -16.8426593));
		big.addPoint(new Point2D(-109, -16.80126713));
		big.addPoint(new Point2D(-108, -16.75931903));
		big.addPoint(new Point2D(-107, -16.71680399));
		big.addPoint(new Point2D(-106, -16.67371071));
		big.addPoint(new Point2D(-105, -16.63002758));
		big.addPoint(new Point2D(-104, -16.58574264));
		big.addPoint(new Point2D(-103, -16.54084362));
		big.addPoint(new Point2D(-102, -16.49531787));
		big.addPoint(new Point2D(-101, -16.44915239));
		big.addPoint(new Point2D(-100, -16.4023338));
		big.addPoint(new Point2D(-99, -16.35484832));
		big.addPoint(new Point2D(-98, -16.30668176));
		big.addPoint(new Point2D(-97, -16.2578195));
		big.addPoint(new Point2D(-96, -16.20824649));
		big.addPoint(new Point2D(-95, -16.15794721));
		big.addPoint(new Point2D(-94, -16.10690565));
		big.addPoint(new Point2D(-93, -16.0551053));
		big.addPoint(new Point2D(-92, -16.00252914));
		big.addPoint(new Point2D(-91, -15.9491596));
		big.addPoint(new Point2D(-90, -15.89497852));
		big.addPoint(new Point2D(-89, -15.83996719));
		big.addPoint(new Point2D(-88, -15.78410624));
		big.addPoint(new Point2D(-87, -15.72737567));
		big.addPoint(new Point2D(-86, -15.66975479));
		big.addPoint(new Point2D(-85, -15.61122223));
		big.addPoint(new Point2D(-84, -15.55175584));
		big.addPoint(new Point2D(-83, -15.49133273));
		big.addPoint(new Point2D(-82, -15.42992918));
		big.addPoint(new Point2D(-81, -15.36752062));
		big.addPoint(new Point2D(-80, -15.30408158));
		big.addPoint(new Point2D(-79, -15.23958567));
		big.addPoint(new Point2D(-78, -15.17400549));
		big.addPoint(new Point2D(-77, -15.10731262));
		big.addPoint(new Point2D(-76, -15.03947757));
		big.addPoint(new Point2D(-75, -14.97046967));
		big.addPoint(new Point2D(-74, -14.90025707));
		big.addPoint(new Point2D(-73, -14.82880663));
		big.addPoint(new Point2D(-72, -14.7560839));
		big.addPoint(new Point2D(-71, -14.682053));
		big.addPoint(new Point2D(-70, -13.91715967));
		big.addPoint(new Point2D(-69, -13.52236677));
		big.addPoint(new Point2D(-68, -13.13325434));
		big.addPoint(new Point2D(-67, -12.74982239));
		big.addPoint(new Point2D(-66, -12.37207091));
		big.addPoint(new Point2D(-65, -11.99999991));
		big.addPoint(new Point2D(-64, -11.63360937));
		big.addPoint(new Point2D(-63, -11.27289932));
		big.addPoint(new Point2D(-62, -10.91786973));
		big.addPoint(new Point2D(-61, -10.56852062));
		big.addPoint(new Point2D(-60, -10.22485198));
		big.addPoint(new Point2D(-59, -9.886863812));
		big.addPoint(new Point2D(-58, -9.55455612));
		big.addPoint(new Point2D(-57, -9.227928901));
		big.addPoint(new Point2D(-56, -8.906982155));
		big.addPoint(new Point2D(-55, -8.591715883));
		big.addPoint(new Point2D(-54, -8.282130084));
		big.addPoint(new Point2D(-53, -7.978224759));
		big.addPoint(new Point2D(-52, -7.679999907));
		big.addPoint(new Point2D(-51, -7.387455528));
		big.addPoint(new Point2D(-50, -7.100591623));
		big.addPoint(new Point2D(-49, -6.819408191));
		big.addPoint(new Point2D(-48, -6.543905232));
		big.addPoint(new Point2D(-47, -6.274082747));
		big.addPoint(new Point2D(-46, -6.009940735));
		big.addPoint(new Point2D(-45, -5.751479197));
		big.addPoint(new Point2D(-44, -5.498698132));
		big.addPoint(new Point2D(-43, -5.25159754));
		big.addPoint(new Point2D(-42, -5.010177422));
		big.addPoint(new Point2D(-41, -4.774437777));
		big.addPoint(new Point2D(-40, -4.544378605));
		big.addPoint(new Point2D(-39, -4.319999907));
		big.addPoint(new Point2D(-38, -4.101301682));
		big.addPoint(new Point2D(-37, -3.88828393));
		big.addPoint(new Point2D(-36, -3.680946652));
		big.addPoint(new Point2D(-35, -3.479289848));
		big.addPoint(new Point2D(-34, -3.283313516));
		big.addPoint(new Point2D(-33, -3.093017658));
		big.addPoint(new Point2D(-32, -2.908402274));
		big.addPoint(new Point2D(-31, -2.729467362));
		big.addPoint(new Point2D(-30, -2.556212925));
		big.addPoint(new Point2D(-29, -2.38863896));
		big.addPoint(new Point2D(-28, -2.226745469));
		big.addPoint(new Point2D(-27, -2.070532451));
		big.addPoint(new Point2D(-26, -1.919999907));
		big.addPoint(new Point2D(-25, -1.775147836));
		big.addPoint(new Point2D(-24, -1.635976238));
		big.addPoint(new Point2D(-23, -1.502485114));
		big.addPoint(new Point2D(-22, -1.374674463));
		big.addPoint(new Point2D(-21, -1.252544285));
		big.addPoint(new Point2D(-20, -1.136094581));
		big.addPoint(new Point2D(-19, -1.025325351));
		big.addPoint(new Point2D(-18, -0.920236593));
		big.addPoint(new Point2D(-17, -0.820828309));
		big.addPoint(new Point2D(-16, -0.727100498));
		big.addPoint(new Point2D(-15, -0.639053161));
		big.addPoint(new Point2D(-14, -0.556686297));
		big.addPoint(new Point2D(-13, -0.479999907));
		big.addPoint(new Point2D(-12, -0.40899399));
		big.addPoint(new Point2D(-11, -0.343668546));
		big.addPoint(new Point2D(-10, -0.284023575));
		big.addPoint(new Point2D(-9, -0.230059078));
		big.addPoint(new Point2D(-8, -0.181775055));
		big.addPoint(new Point2D(-7, -0.139171504));
		big.addPoint(new Point2D(-6, -0.102248427));
		big.addPoint(new Point2D(-5, -0.071005824));
		big.addPoint(new Point2D(-4, -0.045443694));
		big.addPoint(new Point2D(-3, -0.025562037));
		big.addPoint(new Point2D(-2, -0.011360854));
		big.addPoint(new Point2D(-1, -0.002840143));
		big.addPoint(new Point2D(0, 0));
		big.addPoint(new Point2D(1, -0.002840143));
		big.addPoint(new Point2D(2, -0.011360854));
		big.addPoint(new Point2D(3, -0.025562037));
		big.addPoint(new Point2D(4, -0.045443694));
		big.addPoint(new Point2D(5, -0.071005824));
		big.addPoint(new Point2D(6, -0.102248427));
		big.addPoint(new Point2D(7, -0.139171504));
		big.addPoint(new Point2D(8, -0.181775055));
		big.addPoint(new Point2D(9, -0.230059078));
		big.addPoint(new Point2D(10, -0.284023575));
		big.addPoint(new Point2D(11, -0.343668546));
		big.addPoint(new Point2D(12, -0.40899399));
		big.addPoint(new Point2D(13, -0.479999907));
		big.addPoint(new Point2D(14, -0.556686297));
		big.addPoint(new Point2D(15, -0.639053161));
		big.addPoint(new Point2D(16, -0.727100498));
		big.addPoint(new Point2D(17, -0.820828309));
		big.addPoint(new Point2D(18, -0.920236593));
		big.addPoint(new Point2D(19, -1.025325351));
		big.addPoint(new Point2D(20, -1.136094581));
		big.addPoint(new Point2D(21, -1.252544285));
		big.addPoint(new Point2D(22, -1.374674463));
		big.addPoint(new Point2D(23, -1.502485114));
		big.addPoint(new Point2D(24, -1.635976238));
		big.addPoint(new Point2D(25, -1.775147836));
		big.addPoint(new Point2D(26, -1.919999907));
		big.addPoint(new Point2D(27, -2.070532451));
		big.addPoint(new Point2D(28, -2.226745469));
		big.addPoint(new Point2D(29, -2.38863896));
		big.addPoint(new Point2D(30, -2.556212925));
		big.addPoint(new Point2D(31, -2.729467362));
		big.addPoint(new Point2D(32, -2.908402274));
		big.addPoint(new Point2D(33, -3.093017658));
		big.addPoint(new Point2D(34, -3.283313516));
		big.addPoint(new Point2D(35, -3.479289848));
		big.addPoint(new Point2D(36, -3.680946652));
		big.addPoint(new Point2D(37, -3.88828393));
		big.addPoint(new Point2D(38, -4.101301682));
		big.addPoint(new Point2D(39, -4.319999907));
		big.addPoint(new Point2D(40, -4.544378605));
		big.addPoint(new Point2D(41, -4.774437777));
		big.addPoint(new Point2D(42, -5.010177422));
		big.addPoint(new Point2D(43, -5.25159754));
		big.addPoint(new Point2D(44, -5.498698132));
		big.addPoint(new Point2D(45, -5.751479197));
		big.addPoint(new Point2D(46, -6.009940735));
		big.addPoint(new Point2D(47, -6.274082747));
		big.addPoint(new Point2D(48, -6.543905232));
		big.addPoint(new Point2D(49, -6.819408191));
		big.addPoint(new Point2D(50, -7.100591623));
		big.addPoint(new Point2D(51, -7.387455528));
		big.addPoint(new Point2D(52, -7.679999907));
		big.addPoint(new Point2D(53, -7.978224759));
		big.addPoint(new Point2D(54, -8.282130084));
		big.addPoint(new Point2D(55, -8.591715883));
		big.addPoint(new Point2D(56, -8.906982155));
		big.addPoint(new Point2D(57, -9.227928901));
		big.addPoint(new Point2D(58, -9.55455612));
		big.addPoint(new Point2D(59, -9.886863812));
		big.addPoint(new Point2D(60, -10.22485198));
		big.addPoint(new Point2D(61, -10.56852062));
		big.addPoint(new Point2D(62, -10.91786973));
		big.addPoint(new Point2D(63, -11.27289932));
		big.addPoint(new Point2D(64, -11.63360937));
		big.addPoint(new Point2D(65, -11.99999991));
		big.addPoint(new Point2D(66, -12.37207091));
		big.addPoint(new Point2D(67, -12.74982239));
		big.addPoint(new Point2D(68, -13.13325434));
		big.addPoint(new Point2D(69, -13.52236677));
		big.addPoint(new Point2D(70, -13.91715967));
		big.addPoint(new Point2D(71, -14.682053));
		big.addPoint(new Point2D(72, -14.7560839));
		big.addPoint(new Point2D(73, -14.82880663));
		big.addPoint(new Point2D(74, -14.90025707));
		big.addPoint(new Point2D(75, -14.97046967));
		big.addPoint(new Point2D(76, -15.03947757));
		big.addPoint(new Point2D(77, -15.10731262));
		big.addPoint(new Point2D(78, -15.17400549));
		big.addPoint(new Point2D(79, -15.23958567));
		big.addPoint(new Point2D(80, -15.30408158));
		big.addPoint(new Point2D(81, -15.36752062));
		big.addPoint(new Point2D(82, -15.42992918));
		big.addPoint(new Point2D(83, -15.49133273));
		big.addPoint(new Point2D(84, -15.55175584));
		big.addPoint(new Point2D(85, -15.61122223));
		big.addPoint(new Point2D(86, -15.66975479));
		big.addPoint(new Point2D(87, -15.72737567));
		big.addPoint(new Point2D(88, -15.78410624));
		big.addPoint(new Point2D(89, -15.83996719));
		big.addPoint(new Point2D(90, -15.89497852));
		big.addPoint(new Point2D(91, -15.9491596));
		big.addPoint(new Point2D(92, -16.00252914));
		big.addPoint(new Point2D(93, -16.0551053));
		big.addPoint(new Point2D(94, -16.10690565));
		big.addPoint(new Point2D(95, -16.15794721));
		big.addPoint(new Point2D(96, -16.20824649));
		big.addPoint(new Point2D(97, -16.2578195));
		big.addPoint(new Point2D(98, -16.30668176));
		big.addPoint(new Point2D(99, -16.35484832));
		big.addPoint(new Point2D(100, -16.4023338));
		big.addPoint(new Point2D(101, -16.44915239));
		big.addPoint(new Point2D(102, -16.49531787));
		big.addPoint(new Point2D(103, -16.54084362));
		big.addPoint(new Point2D(104, -16.58574264));
		big.addPoint(new Point2D(105, -16.63002758));
		big.addPoint(new Point2D(106, -16.67371071));
		big.addPoint(new Point2D(107, -16.71680399));
		big.addPoint(new Point2D(108, -16.75931903));
		big.addPoint(new Point2D(109, -16.80126713));
		big.addPoint(new Point2D(110, -16.8426593));
		big.addPoint(new Point2D(111, -16.88350624));
		big.addPoint(new Point2D(112, -16.92381838));
		big.addPoint(new Point2D(113, -16.96360587));
		big.addPoint(new Point2D(114, -17.00287859));
		big.addPoint(new Point2D(115, -17.04164618));
		big.addPoint(new Point2D(116, -17.07991802));
		big.addPoint(new Point2D(117, -17.11770326));
		big.addPoint(new Point2D(118, -17.15501081));
		big.addPoint(new Point2D(119, -17.19184938));
		big.addPoint(new Point2D(120, -17.22822744));
		big.addPoint(new Point2D(121, -17.26415326));
		big.addPoint(new Point2D(122, -17.29963491));
		big.addPoint(new Point2D(123, -17.33468027));
		big.addPoint(new Point2D(124, -17.36929702));
		big.addPoint(new Point2D(125, -17.40349267));
		big.addPoint(new Point2D(126, -17.43727453));
		big.addPoint(new Point2D(127, -17.47064977));
		big.addPoint(new Point2D(128, -17.50362536));
		big.addPoint(new Point2D(129, -17.53620813));
		big.addPoint(new Point2D(130, -17.56840475));
		big.addPoint(new Point2D(131, -17.60022174));
		big.addPoint(new Point2D(132, -17.63166545));
		big.addPoint(new Point2D(133, -17.66274211));
		big.addPoint(new Point2D(134, -17.69345781));
		big.addPoint(new Point2D(135, -17.7238185));
		big.addPoint(new Point2D(136, -17.75382999));
		big.addPoint(new Point2D(137, -17.78349797));
		big.addPoint(new Point2D(138, -17.81282802));
		big.addPoint(new Point2D(139, -17.84182557));
		big.addPoint(new Point2D(140, -17.87049595));
		big.addPoint(new Point2D(141, -17.89884439));
		big.addPoint(new Point2D(142, -17.92687597));
		big.addPoint(new Point2D(143, -17.95459571));
		big.addPoint(new Point2D(144, -17.98200849));
		big.addPoint(new Point2D(145, -18.0091191));
		big.addPoint(new Point2D(146, -18.03593222));
		big.addPoint(new Point2D(147, -18.06245247));
		big.addPoint(new Point2D(148, -18.08868432));
		big.addPoint(new Point2D(149, -18.1146322));
		big.addPoint(new Point2D(150, -18.14030042));
		big.addPoint(new Point2D(151, -18.16569321));
		big.addPoint(new Point2D(152, -18.19081472));
		big.addPoint(new Point2D(153, -18.21566901));
		big.addPoint(new Point2D(154, -18.24026007));
		big.addPoint(new Point2D(155, -18.2645918));
		big.addPoint(new Point2D(156, -18.28866804));
		big.addPoint(new Point2D(157, -18.31249254));
		big.addPoint(new Point2D(158, -18.33606897));
		big.addPoint(new Point2D(159, -18.35940096));
		big.addPoint(new Point2D(160, -18.38249204));
		big.addPoint(new Point2D(161, -18.40534569));
		big.addPoint(new Point2D(162, -18.42796531));
		big.addPoint(new Point2D(163, -18.45035426));
		big.addPoint(new Point2D(164, -18.47251582));
		big.addPoint(new Point2D(165, -18.49445319));
		big.addPoint(new Point2D(166, -18.51616956));
		big.addPoint(new Point2D(167, -18.53766801));
		big.addPoint(new Point2D(168, -18.55895159));
		big.addPoint(new Point2D(169, -18.5800233));
		big.addPoint(new Point2D(170, -18.60088607));
		big.addPoint(new Point2D(171, -18.62154278));
		big.addPoint(new Point2D(172, -18.64199626));
		big.addPoint(new Point2D(173, -18.66224929));
		big.addPoint(new Point2D(174, -18.6823046));
		big.addPoint(new Point2D(175, -18.70216488));
		big.addPoint(new Point2D(176, -18.72183276));
		big.addPoint(new Point2D(177, -18.74131082));
		big.addPoint(new Point2D(178, -18.76060161));
		big.addPoint(new Point2D(179, -18.77970763));
		big.addPoint(new Point2D(180, -18.77970763));
		return big;
	}
}