package com.tcl.pili;

import java.awt.image.BufferedImage;

final class TypesetText implements Runnable {
	private BufferedImage plotImage;
	private OnTextTypesetListener listener;
	
	public TypesetText(BufferedImage plotImage, OnTextTypesetListener listener) {
		this.plotImage = plotImage;
		this.listener = listener;
	}
	
	public void run() {
		TypesetMethodInterface method = new TypesetMethod();
		method.setParameter(prepareTypesetParameter());
		
		listener.onTextTypeset(method.typeset(plotImage));
	}
	
	private TypesetParamter prepareTypesetParameter() {
		TypesetParamter param = new TypesetParamter();
		
		param.set(TypesetParamter.KEY_TOP_MARGIN, 30);
		param.set(TypesetParamter.KEY_BOTTOM_MARGIN, 30);
		param.set(TypesetParamter.KEY_LEFT_MARGIN, 30);
		param.set(TypesetParamter.KEY_RIGHT_MARGIN, 30);
		param.set(TypesetParamter.KEY_LINES_PER_PAGE, 20);
		param.set(TypesetParamter.KEY_LINE_SPACE, 6);
		param.set(TypesetParamter.KEY_WORDS_PER_LINE, 15);
		param.set(TypesetParamter.KEY_WORD_SPACE, 3);
		
		return param;
	}
}