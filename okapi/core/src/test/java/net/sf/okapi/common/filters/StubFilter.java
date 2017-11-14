package net.sf.okapi.common.filters;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

/**
 * User: Christian Hargraves
 * Date: Jun 26, 2009
 * Time: 12:41:47 PM
 */
public class StubFilter implements IFilter {

	private static final String MIMETYPE = "text/foo";

	private EncoderManager encoderManager;
	
    public String getName() {
        return "foobar";
    }
	
	public String getDisplayName () {
		return "Stub Filter";
	}

    public void open(RawDocument input) {
    }

    public void open(RawDocument input, boolean generateSkeleton) {
    }

    public void close() {
    }

    public boolean hasNext() {
        return false;
    }

    public Event next() {
        return null;
    }
    public void cancel() {
    }
    public IParameters getParameters() {
        return new ParametersStub();
    }

    public void setParameters(IParameters params) {
    }

	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
	}

    public ISkeletonWriter createSkeletonWriter() {
        return null;
    }

    public IFilterWriter createFilterWriter() {
        return null;
    }

    public String getMimeType() {
        return MIMETYPE;
    }

    public List<FilterConfiguration> getConfigurations() {
 		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			MIMETYPE,
			getClass().getName(),
			"Regex Default",
			"Default foo configuration."));
		list.add(new FilterConfiguration(getName()+"-srt",
			MIMETYPE,
			getClass().getName(),
			"STR Sub-Titles",
			"Configuration for SRT (Sub-Rip Text) sub-titles files.",
                "srt.fprm"));
		return list;
    }

	public EncoderManager getEncoderManager () {
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			encoderManager.setAllKnownMappings();
		}
		return encoderManager;
	}

	class ParametersStub implements IParameters{

        public void reset() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void fromString(String data) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void load(URL inputURL, boolean ignoreErrors) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
        
        @Override
		public void load(InputStream inStream, boolean ignoreErrors) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

        public void save(String filePath) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getPath() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setPath(String filePath) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean getBoolean(String name) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getString(String name) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public int getInteger(String name) {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public ParametersDescription getParametersDescription() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

		@Override
		public void setBoolean (String name, boolean value) {
			// TODO Auto-generated method stub
		}

		@Override
		public void setInteger (String name, int value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setString (String name, String value) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public String toString () {
			return "Dummy filter custom filter settings file";
		}
    }

}
