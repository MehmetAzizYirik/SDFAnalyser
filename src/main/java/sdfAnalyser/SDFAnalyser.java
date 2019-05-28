/**
 * MIT License
 *
 * Copyright (c) 2019 Mehmet Aziz Yirik
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


/**
 * This class is for comparing the list of molecules given in an sdf file.
 * 
 * @author Mehmet Aziz Yirik
 */


package sdfAnalyser;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import org.openbabel.OBConversion;
//import org.openbabel.OBMol;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
//import org.openscience.cdk.qsar.descriptors.molecular.AtomCountDescriptor;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

public class SDFAnalyser {
	public static boolean verbose = false;
	public static String directory;
	public static IChemObjectBuilder builder =SilentChemObjectBuilder.getInstance();
	
	/**
	 * This is the InChi generator from CDK.
	 */
	
	public static String inchiGeneration(IAtomContainer molecule) throws CDKException {
		String inchi = InChIGeneratorFactory.getInstance().getInChIGenerator(molecule).getInchi();	
		return inchi;
	}
	
	/**
	 * Open Babel Inchi Generation is used in this class.
	 */
	// TODO: How to stop error: *** Open Babel Warning  in InChI code #0 :Omitted undefined stereo
	/**public static List<String> getInchis(String path) throws CloneNotSupportedException, CDKException, IOException{
		List<String> inchis= new ArrayList<String>();
		System.loadLibrary("openbabel_java");
		// Read molecule from SMILES string
		OBConversion conv = new OBConversion();
		OBMol mol = new OBMol();
		conv.SetInAndOutFormats("sdf","inchi");
		boolean input=conv.ReadFile(mol, path);
		while (input) {
			inchis.add(conv.WriteString(mol).replace("InChI=", ""));
			mol = new OBMol();
			input = conv.Read(mol);
		}
		return inchis;
	}**/
	

	/**
	 * Reading the sdf file and creating acontainers.
	 */
	
	/**public static ListMultimap<String, IAtomContainer> getAtomContainerInchiMap( String path) throws CDKException, IOException, CloneNotSupportedException {
		ListMultimap<String, IAtomContainer> map = ArrayListMultimap.create();
		IteratingSDFReader iterator = new IteratingSDFReader(new FileReader(path),SilentChemObjectBuilder.getInstance());
		int i=0;
		List<String> inchis=getInchis(path);
		while (iterator.hasNext()) {
			IAtomContainer ac=iterator.next();
			map.put(inchis.get(i),ac);
			i++;
		}
		iterator.close();
	return map;
	}**/
	
	
	/**
	 * Molecule depiction generator
	 */
	
	public static void depict(IAtomContainer molecule, String path) throws CloneNotSupportedException, CDKException, IOException{
		DepictionGenerator depiction = new DepictionGenerator();
		depiction.withCarbonSymbols().withSize(200, 200).withZoom(4).depict(molecule).writeTo(path);
	}
	
	public static List<List<Integer>> getHydrogenDistributions( String path) throws CDKException, IOException, CloneNotSupportedException {
		IteratingSDFReader iterator = new IteratingSDFReader(new FileReader(path),SilentChemObjectBuilder.getInstance());
		List<List<Integer>> distributions= new ArrayList<List<Integer>>();
		while (iterator.hasNext()) {
			IAtomContainer ac=iterator.next();
			List<Integer> hydrogens= new ArrayList<Integer>();
			for(IAtom atom: ac.atoms()) {
				hydrogens.add(AtomContainerManipulator.countHydrogens(ac, atom));
			}
			if(!distributions.contains(hydrogens)) {
				System.out.println(hydrogens);
				distributions.add(hydrogens);
			}
		}
		iterator.close();
		return distributions;
	} 
	public static void getDepicts( String path) throws CDKException, IOException, CloneNotSupportedException {
		IteratingSDFReader iterator = new IteratingSDFReader(new FileReader(path),SilentChemObjectBuilder.getInstance());
		int count=0;
		while (iterator.hasNext()) {
			IAtomContainer ac=iterator.next();
			depict(ac,"C:\\Users\\mehme\\Desktop\\depict\\same.png");
			count=count+1;
		}
		iterator.close();
	} 
	/**public static  IDescriptorResult hydrogenCounter(IAtomContainer ac, String type) throws CDKException {
		Object[] parameter = { type };
		
		AtomCountDescriptor atomcount = new AtomCountDescriptor();

		atomcount.setParameters(parameter);
		DescriptorValue value=atomcount.calculate(ac);
		return value.getValue();
	}**/
	
	
	void parseArguments(String[] arguments) throws ParseException
	{
		Options options = setupOptions(arguments);	
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse(options, arguments);
			String directory = cmd.getOptionValue("directory");
			SDFAnalyser.directory=directory;
			if (cmd.hasOption("verbose")) SDFAnalyser.verbose = true;
		
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			HelpFormatter formatter = new HelpFormatter();
			formatter.setOptionComparator(null);
			String header = "\nFor a molecular formula, it calculates all the possible hydrogen distributions to the atoms.";
			String footer = "\nPlease report issues at https://github.com/MehmetAzizYirik/HydrogenDistributor";
			formatter.printHelp( "java -jar HydrogenDistributor.jar", header, options, footer, true );
			throw new ParseException("Problem parsing command line");
		}
	}
	
	private Options setupOptions(String[] arguments)
	{
		Options options = new Options();
		Option formula = Option.builder("d")
			     .required(true)
			     .hasArg()
			     .longOpt("directory")
			     .desc("The directory path for the input SDF file (required)")
			     .build();
		options.addOption(formula);	
		Option verbose = Option.builder("v")
			     .required(false)
			     .longOpt("verbose")
			     .desc("Print messages about the distributor")
			     .build();
		options.addOption(verbose);	
		return options;
	}
	
	public static void main(String[] arguments) throws CDKException, IOException, CloneNotSupportedException {
		//mol2inchi input= new mol2inchi();
		List<List<Integer>> distributions=getHydrogenDistributions("C:\\Users\\mehme\\Desktop\\Current\\molgentestfiles\\molgenC6H9O6PS.sdf");
		System.out.println(distributions.size());
		//String[] arguments1= {"-d","C:\\Users\\mehme\\Desktop\\molgenC9H12OexplH.sdf","-v"};
		/**try {
			input.parseArguments(arguments1);
		} catch (Exception e) {
			if (mol2inchi.verbose) e.getCause(); 
		}**/
		/**SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        IAtomContainer mol = sp.parseSmiles("c1ccccc1"); // benzol
		SmilesGenerator sg	= new SmilesGenerator(SmiFlavor.Generic);
		String	smi  = sg.create(mol); // CCO, C(C)O, C(O)C, or OCC
		System.out.println(smi);**/
	}
}
