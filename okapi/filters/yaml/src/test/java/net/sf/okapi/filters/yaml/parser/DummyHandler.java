package net.sf.okapi.filters.yaml.parser;

public class DummyHandler implements IYamlHandler {

	@Override
	public void handleStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleComment(String c, boolean insideScalar) {
		int x = 1;
	}
	
	@Override
	public void handleWhitespace(String whitespace, boolean insideScalar) {
		int x = 1;
	}

	@Override
	public void handleKey(Key key) {
		int x = 1;
	}

	@Override
	public void handleScalar(Scalar scalar) {
		int x = 1;
	}

	@Override
	public void handleMapStart(boolean flow) {
		int x = 1;
	}

	@Override
	public void handleMapEnd(boolean flow) {
		int x = 1;
	}

	@Override
	public void handleSequenceStart(boolean flow) {
		int x = 1;
	}

	@Override
	public void handleSequenceEnd(boolean flow) {
		int x = 1;
	}

	@Override
	public void handleMarker(String marker) {
		int x = 1;
	}

	@Override
	public void handleOther(String other) {
		int x = 1;
	}

	@Override
	public void handleDocumentStart(String start) {
		int x = 1;
	}

	@Override
	public void handleDocumentEnd(String end) {
		int x = 1;
	}

	@Override
	public void handleMappingElementEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleBlockSequenceNodeStart(String dash, int indent) {
		// TODO Auto-generated method stub
		
	}
}
