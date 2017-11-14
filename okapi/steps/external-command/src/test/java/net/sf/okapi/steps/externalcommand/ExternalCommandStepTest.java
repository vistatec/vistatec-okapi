package net.sf.okapi.steps.externalcommand;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.net.URL;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.pipeline.Pipeline;
import net.sf.okapi.common.resource.RawDocument;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ExternalCommandStepTest {
	private Pipeline pipeline;
	private ExternalCommandStep externalCommand;	

	@Before
	public void setUp() throws Exception {
		// create pipeline
		pipeline = new Pipeline();

		// configure ExternalCommandStep
		externalCommand = new ExternalCommandStep();

		pipeline.addStep(externalCommand);
	}

	@After
	public void tearDown() throws Exception {
		pipeline.destroy();
	}

	@Test
	public void sortCommand() throws URISyntaxException {
		Parameters p = new Parameters();
		switch (Util.getOS()) {
		case WINDOWS:
			p.setCommand("sort ${inputPath} /O ${outputPath}");
			break;
		case MAC:
			p.setCommand("sort ${inputPath} -o ${outputPath}");
			break;
		case LINUX:
			p.setCommand("sort ${inputPath} -o ${outputPath}");
			break;
		}
		p.setTimeout(60);
		externalCommand.setParameters(p);

		URL url = ExternalCommandStepTest.class.getResource("/test.txt");
		RawDocument d = new RawDocument(url.toURI(), "UTF-8", LocaleId.ENGLISH, LocaleId.CHINA_CHINESE);

		pipeline.startBatch();

		pipeline.process(d);

		pipeline.endBatch();
	}
	
	@Test
	public void commandStringParseTest() {
		String cmd = " sort  \"/path with/spaces in/it\"    /path\\ with/escaped\\ spaces/"
				+ " \"escape\\\"escape\" 'noescape\\'noescape'' \"noescape\\ noescape\""
				+ " C:\\windows\\path";
		String[] args = ExternalCommandStep.splitCommand(cmd);
		assertEquals("/path with/spaces in/it", args[1]);
		assertEquals("/path with/escaped spaces/", args[2]);
		assertEquals("escape\"escape", args[3]);
		assertEquals("noescape\\noescape", args[4]);
		assertEquals("noescape\\ noescape", args[5]);
		assertEquals("C:\\windows\\path", args[6]);
		assertEquals(args.length, 7);
		args = ExternalCommandStep.splitCommand(" ");
		assertEquals(args[0], "");
		assertEquals(args.length, 1);
	}
}
