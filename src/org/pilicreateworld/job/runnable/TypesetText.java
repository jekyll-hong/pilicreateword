package org.pilicreateworld.job.runnable;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

import org.pilicreateworld.typeset.SimpleTypeset;
import org.pilicreateworld.typeset.TypesetParamter;

public class TypesetText implements Runnable {
	private BufferedImage image;
	private String device;
	private OnTextTypesetListener listener;
	
	public TypesetText(BufferedImage image, String device, OnTextTypesetListener listener) {
		this.image = image;
		this.device = device;
		this.listener = listener;
	}
	
	public void run() {
		TypesetParamter param = new TypesetParamter();
		setDeviceParameter(param);
		
		SimpleTypeset module = new SimpleTypeset();
		LinkedList<BufferedImage> words = module.extract(image);
		LinkedList<BufferedImage> pages = module.typeset(words, param);
		
		listener.onTextTypeset(pages);
	}
	
	private void setDeviceParameter(TypesetParamter param) {
		if (device.equals("phone")) {
			param.set(TypesetParamter.KEY_TOP_MARGIN, 15);
			param.set(TypesetParamter.KEY_BOTTOM_MARGIN, 15);
			param.set(TypesetParamter.KEY_LEFT_MARGIN, 30);
			param.set(TypesetParamter.KEY_RIGHT_MARGIN, 30);
			param.set(TypesetParamter.KEY_LINES_PER_PAGE, 20);
			param.set(TypesetParamter.KEY_LINE_SPACE, 6);
			param.set(TypesetParamter.KEY_WORDS_PER_LINE, 15);
			param.set(TypesetParamter.KEY_WORD_SPACE, 3);
		}
		else if (device.equals("tablet")) {
			//TODO: set parameter for tablet
			System.out.print("not support for tablet!\r\n");
		}
		else if (device.equals("laptop")) {
			//TODO: set parameter for laptop
			System.out.print("not support for laptop!\r\n");
		}
		else if (device.equals("desktop")) {
			//TODO: set parameter for desktop
			System.out.print("not support for desktop!\r\n");
		}
		else {
			System.err.print("illegal device!\r\n");
		}
	}
}