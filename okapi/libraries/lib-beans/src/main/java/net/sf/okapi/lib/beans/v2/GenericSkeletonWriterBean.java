package net.sf.okapi.lib.beans.v2;

import java.util.HashMap;
import java.util.Map;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filters.SubFilterSkeletonWriter;
import net.sf.okapi.common.layerprovider.ILayerProvider;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.Referent;
import net.sf.okapi.lib.beans.v1.LocaleIdBean;
import net.sf.okapi.lib.persistence.BeanMap;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.lib.persistence.beans.FactoryBean;
import net.sf.okapi.lib.persistence.beans.TypeInfoBean;

public class GenericSkeletonWriterBean extends
		PersistenceBean<GenericSkeletonWriter> {

	private LocaleIdBean inputLoc = new LocaleIdBean();
	private LocaleIdBean outputLoc = new LocaleIdBean();
//	private LayerProviderBean layer = new LayerProviderBean();
	private TypeInfoBean layer = new TypeInfoBean();	
//	private EncoderManagerBean encoderManager = new EncoderManagerBean();
//	private FactoryBean encoderManager = new FactoryBean();
	private FactoryBean encoderManager = new FactoryBean();
	private boolean isMultilingual;
	private boolean allowEmptyOutputTarget;

	private Map<String, FactoryBean> referents = new HashMap<String, FactoryBean>();
	private String outputEncoding;
	private int referentCopies; 
	private FactoryBean sfWriter = new FactoryBean(); 
	
	@Override
	protected GenericSkeletonWriter createObject(IPersistenceSession session) {
//		return new GenericSkeletonWriter(
//				inputLoc.get(LocaleId.class, session),
//				outputLoc.get(LocaleId.class, session),
//				layer.get(ILayerProvider.class, session),
//				encoderManager.get(EncoderManager.class, session),
//				isMultilingual,
//				allowEmptyOutputTarget,
//				BeanMap.get(referents, Referent.class, session),
//				outputEncoding,
//				referentCopies, 
//				sfWriter.get(ISkeletonWriter.class, session)
//				);
		return new GenericSkeletonWriter();
	}

	@Override
	protected void setObject(GenericSkeletonWriter obj,
			IPersistenceSession session) {
		obj.setInputLoc(inputLoc.get(LocaleId.class, session));
		obj.setOutputLoc(outputLoc.get(LocaleId.class, session));
		obj.setLayer(layer.get(ILayerProvider.class, session));
		obj.setEncoderManager(encoderManager.get(EncoderManager.class, session));
		obj.setMultilingual(isMultilingual);
		obj.setAllowEmptyOutputTarget(allowEmptyOutputTarget);
		obj.setReferents(BeanMap.get(referents, Referent.class, session));
		obj.setOutputEncoding(outputEncoding);
		obj.setReferentCopies(referentCopies);
		obj.setSfWriter(sfWriter.get(SubFilterSkeletonWriter.class, session));
	}

	@Override
	protected void fromObject(GenericSkeletonWriter obj,
			IPersistenceSession session) {
		inputLoc.set(obj.getInputLoc(), session);
		outputLoc.set(obj.getOutputLoc(), session);
		layer.set(obj.getLayer(), session);
		encoderManager.set(obj.getEncoderManager(), session);
		isMultilingual = obj.isMultilingual();
		allowEmptyOutputTarget = obj.isAllowEmptyOutputTarget();
		BeanMap.set(referents, FactoryBean.class, obj.getReferents(), session);
		outputEncoding = obj.getOutputEncoding();
		referentCopies = obj.getReferentCopies();
		sfWriter.set(obj.getSfWriter(), session);
	}

	public final LocaleIdBean getInputLoc() {
		return inputLoc;
	}

	public final void setInputLoc(LocaleIdBean inputLoc) {
		this.inputLoc = inputLoc;
	}

	public final LocaleIdBean getOutputLoc() {
		return outputLoc;
	}

	public final void setOutputLoc(LocaleIdBean outputLoc) {
		this.outputLoc = outputLoc;
	}

	public final TypeInfoBean getLayer() {
		return layer;
	}

	public final void setLayer(TypeInfoBean layer) {
		this.layer = layer;
	}

	public final FactoryBean getEncoderManager() {
		return encoderManager;
	}

	public final void setEncoderManager(FactoryBean encoderManager) {
		this.encoderManager = encoderManager;
	}

	public final boolean isMultilingual() {
		return isMultilingual;
	}

	public final void setMultilingual(boolean isMultilingual) {
		this.isMultilingual = isMultilingual;
	}

	public final boolean isAllowEmptyOutputTarget() {
		return allowEmptyOutputTarget;
	}

	public final void setAllowEmptyOutputTarget(boolean allowEmptyOutputTarget) {
		this.allowEmptyOutputTarget = allowEmptyOutputTarget;
	}

	public final Map<String, FactoryBean> getReferents() {
		return referents;
	}

	public final void setReferents(Map<String, FactoryBean> referents) {
		this.referents = referents;
	}

	public final String getOutputEncoding() {
		return outputEncoding;
	}

	public final void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	public final int getReferentCopies() {
		return referentCopies;
	}

	public final void setReferentCopies(int referentCopies) {
		this.referentCopies = referentCopies;
	}

	public final FactoryBean getSfWriter() {
		return sfWriter;
	}

	public final void setSfWriter(FactoryBean sfWriter) {
		this.sfWriter = sfWriter;
	}
}
