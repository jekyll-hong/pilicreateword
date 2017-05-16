package com.tcl.pili;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

public final class MainApplication {
	private static final String domain = "https://pilicreateworld.tw-blog.com";
	private static final String storage = "/home/hongyu/Downloads/pili";
	
	private static int pageNumber = 0;

	public static void main(String[] args) {
		try {
			parseDomainPage();
		}
		catch (ParserException e) {
			System.err.print("parse domain page error, exception: " + e.getMessage() + "\r\n");
		}
		catch (IOException e) {
			System.err.print("parse domain page error, exception: " + e.getMessage() + "\r\n");
		}
	}
	
	private static void parseDomainPage() throws ParserException, IOException {
		Parser parser = new Parser(createConnection(new URL(domain), true));
		
		//find main page
		Node mainPageNode = parser.parse(null)
				.extractAllNodesThatMatch(new HasAttributeFilter("name", "rbottom2"), true)
				.elementAt(0);
		
		mainPageNode.accept(new NodeVisitor() {
			public void visitTag (Tag tag) {
				String mainPageRelativePath = tag.getAttribute("src");
				if (mainPageRelativePath != null) {
					File piliDir = new File(storage);
					if (!piliDir.exists()) {
						piliDir.mkdirs();
					}
					
					try {
						parseMainPage(domain + "/" + mainPageRelativePath, piliDir.getPath());
					}
					catch (ParserException e) {
						System.err.print("parse main page error, exception: " + e.getMessage() + "\r\n");
					}
					catch (IOException e) {
						System.err.print("parse main page error, exception: " + e.getMessage() + "\r\n");
					}
				}
			}
		});
	}
	
	private static void parseMainPage(String mainPageURL, final String piliDirPath) throws ParserException, IOException {
		Parser parser = new Parser(createConnection(new URL(mainPageURL), true));
		
		//find drama information
		NodeList dramaNodes = parser.parse(null)
				.extractAllNodesThatMatch(new HasAttributeFilter("target", "rbottom2"), true);
		for (int i = 0; i < dramaNodes.size(); i++) {
			Node dramaNode = dramaNodes.elementAt(i);
			
			dramaNode.accept(new NodeVisitor() {
				public void visitTag (Tag tag) {
					String dramaPageRelativePath = tag.getAttribute("href");
					if (dramaPageRelativePath != null) {
						String dramaName = getDramaName(tag.toPlainTextString());
						System.out.print("start saving drama " + dramaName + "\r\n");
						
						File dramaDir = new File(piliDirPath + "/" + dramaName);
						if (!dramaDir.exists()) {
							dramaDir.mkdir();
						}
						
						File pdfFile = new File(dramaDir.getPath() + ".pdf");
						if (pdfFile.exists()) {
							return;
						}
						
						try {
							parseDramaPage(domain + "/" + dramaPageRelativePath, dramaDir.getPath());
							packDramaPDF(enumeratePageImages(dramaDir.getPath() + "/pdf"), dramaDir.getPath() + ".pdf");
							pageNumber = 0; //reset
						}
						catch (ParserException e) {
							System.err.print("parse drama page error, exception: " + e.getMessage() + "\r\n");
						}
						catch (IOException e) {
							System.err.print("pack drama pdf error, exception: " + e.getMessage() + "\r\n");
						}
					}
				}
			});
		}
	}
	
	private static String getDramaName(String text) {
		String serialNumber = "";
		Matcher result = Pattern.compile("\\d{2}.").matcher(text);
		if (result.find()) {
			serialNumber = result.group(0);
		}
		
		return serialNumber + text.substring(text.indexOf("【") + 1, text.lastIndexOf("】"));
	}
	
	private static List<File> enumeratePageImages(String pdfDirPath) {
		File dramaDir = new File(pdfDirPath);
		
		File[] jpgFiles = dramaDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".png");
			}
		});
		
		return sortPageImageByName(Arrays.asList(jpgFiles));
	}
	
	private static List<File> sortPageImageByName(List<File> pageImageFiles) {
		Collections.sort(pageImageFiles, new Comparator<File>() {
            public int compare(File file1, File file2) {
                String name1 = file1.getName();
                String serialNumber1 = name1.substring(0, name1.indexOf("."));
                
                String name2 = file2.getName();
                String serialNumber2 = name2.substring(0, name2.indexOf("."));
                
                return Integer.parseInt(serialNumber1) - Integer.parseInt(serialNumber2);
            }
        });
		
		return pageImageFiles;
	}
	
	private static void packDramaPDF(List<File> pageImageFiles, String dramaPDFPath) throws IOException {
		PdfDocument pdfDoc = new PdfDocument(new PdfWriter(dramaPDFPath));
		
		//first page
		Image image = new Image(ImageDataFactory.create(pageImageFiles.get(0).getPath()));
        Document doc = new Document(pdfDoc, new PageSize(image.getImageWidth(), image.getImageHeight()));
        doc.add(image);
        
        //continuing pages
        for (int i = 1; i < pageImageFiles.size(); i++) {
            image = new Image(ImageDataFactory.create(pageImageFiles.get(i).getPath()));
            pdfDoc.addNewPage(new PageSize(image.getImageWidth(), image.getImageHeight()));
            doc.add(image);
        }
        
        doc.close();
	}
	
	private static void parseDramaPage(String dramaPageURL, final String dramaDirPath) throws ParserException, IOException {
		Parser parser = new Parser(createConnection(new URL(dramaPageURL), true));
		
		//find episode information
		NodeList episodeNodes = parser.parse(null)
				.extractAllNodesThatMatch(new HasAttributeFilter("target", "_blank"), true);
		for (int i = 0; i < episodeNodes.size(); i++) {
			Node episodeNode = episodeNodes.elementAt(i);
			
			episodeNode.accept(new NodeVisitor() {
				public void visitTag (Tag tag) {
					String episodePageRelativePath = tag.getAttribute("href");
					if (episodePageRelativePath != null) {
						String episodeName = getEpisodeName(tag.toPlainTextString());
						System.out.print("start saving episode " + episodeName + "\r\n");
						
						File episodeDir = new File(dramaDirPath + "/" + episodeName);
						if (!episodeDir.exists()) {
							episodeDir.mkdir();
						}
						
						try {
							parseEpisodePage(domain + "/PILI/" + episodePageRelativePath, episodeDir.getPath());
							BufferedImage plotImage = combinePlotImages(enumeratePlotImages(episodeDir.getPath()));
							pageNumber = preparePDFPages(plotImage, dramaDirPath + "/pdf", pageNumber);
						}
						catch (ParserException e) {
							System.err.print("parse episode page error, exception: " + e.getMessage() + "\r\n");
						}
						catch (IOException e) {
							System.err.print("combine episode image error, exception: " + e.getMessage() + "\r\n");
						}
					}
				}
			});
		}
	}
	
	private static final String getEpisodeName(String text) {
		int start = text.indexOf(".") - 2;
		
		int end = text.indexOf("&nbsp;");
		if (end < 0) {
			return text.substring(start);
		}
		else {
			return text.substring(start, end);
		}
	}
	
	private static List<File> enumeratePlotImages(String episodeDirPath) {
		File episodeDir = new File(episodeDirPath);
		
		File[] jpgFiles = episodeDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".gif");
			}
		});
		
		return sortPlotImageByName(Arrays.asList(jpgFiles));
	}
	
	private static List<File> sortPlotImageByName(List<File> plotImageFiles) {
		Collections.sort(plotImageFiles, new Comparator<File>() {
            public int compare(File plotImageFile1, File plotImageFile2) {
                String name1 = plotImageFile1.getName();
                name1 = name1.substring(0, name1.lastIndexOf(".gif"));
                
                String name2 = plotImageFile2.getName();
                name2 = name2.substring(0, name2.lastIndexOf(".gif"));
                
                return Integer.parseInt(name1) - Integer.parseInt(name2);
            }
        });
		
		return plotImageFiles;
	}
	
	private static BufferedImage combinePlotImages(List<File> plotImageFiles) throws IOException {
		if (plotImageFiles.isEmpty()) {
			System.out.print("no plot image in episode dir, skip" + "\r\n");
			return null;
		}
		
		BufferedImage[] images = new BufferedImage[plotImageFiles.size()];
		for (int i = 0; i < plotImageFiles.size(); i++) {
			images[i] = ImageIO.read(plotImageFiles.get(i));
			
			if (i == 0) {
				//first image, crop 30 lines from top
				images[i] = images[i].getSubimage(0, 30, images[i].getWidth(), images[i].getHeight() - 30);
			}
		}
		
		//combined image is gray image
		int combinedImageWidth = images[0].getWidth();
		int combinedImageHeight = 0;
		for (int i = 0; i < images.length; i++) {
			combinedImageHeight += images[i].getHeight();
		}
		BufferedImage combinedImage = new BufferedImage(combinedImageWidth, combinedImageHeight, BufferedImage.TYPE_BYTE_GRAY);
		
		int offsetY = 0;
		for (int i = 0; i < images.length; i++) {
			combinedImage.createGraphics().drawImage(images[i], null, 0, offsetY);
			offsetY += images[i].getHeight();
		}
		
		//improve word-background contrast
		improveContrast(combinedImage);
		
		return combinedImage;
	}
	
	private static void improveContrast(BufferedImage image) {
		//CAUSION: gray from 200 to 250. 210 is word, 250 is background.
		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 0; j < image.getWidth(); j++) {
				int gray = image.getRGB(j, i) & 0xff;
				
				if (gray >= 240) {
					//pure background region
					image.setRGB(j, i, new Color(255, 255, 255).getRGB());
				}
				else if (gray >= 230 && gray < 240) {
					//transitional region
					image.setRGB(j, i, new Color(gray - 10, gray - 10, gray - 10).getRGB());
				}
				else if (gray >= 220 && gray < 230) {
					//transitional region
					image.setRGB(j, i, new Color(gray - 30, gray - 30, gray - 30).getRGB());
				}
				else if (gray < 220) {
					//pure word region
					image.setRGB(j, i, new Color(0, 0, 0).getRGB());
				}
			}
		}
	}
	
	private static int preparePDFPages(BufferedImage image, String pdfDirPath, int pageNumber) throws IOException {
		File pdfDir = new File(pdfDirPath);
		if (!pdfDir.exists()) {
			pdfDir.mkdir();
		}
		
		if (image == null) {
			System.out.print("no plot image in episode dir, skip" + "\r\n");
			return pageNumber;
		}
		
		//word rectangle
		int[] word = wordRect(image);
		int wordWidth = word[3] - word[1] + 1;
		int wordHeight = word[2] - word[0] + 1;
		
		//calculate page parameter by word rectangle
		final int margin = 2 * wordHeight;
		final int space = 2;
		final int wordsInLine = 15;
		final int lineNumber = 25;
		int pageWidth = margin + (wordWidth * wordsInLine) + ((wordsInLine - 1) * space) + margin;
		int pageHeight = margin + (wordHeight * lineNumber) + ((lineNumber - 1) * space) + margin;
		
		//arrange words in image
		boolean newSection = true;
	    ArrayList<int[]> words = wordsInImage(image, wordHeight);
		while (!words.isEmpty()) {
			//create page image
			BufferedImage pageImage = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_BYTE_GRAY);
			
			//fill page image by white color 
			Graphics2D graphics = pageImage.createGraphics();
			graphics.setPaint(new Color(255, 255, 255));
			graphics.fillRect(0, 0, pageImage.getWidth(), pageImage.getHeight());
			
			//place words
			int yPos = margin + 1;
			for (int i = 0; i < lineNumber; i++) {
				if (words.isEmpty()) {
					break;
				}
				
				//if next line is a new section
				if (!newSection) {
					word = words.get(0);
					if (word[0] == -1) {
						words.remove(0);
						//end of this section. next line is a new section
						newSection = true;
					}
				}
				
				int wordsNumber = wordsInLine;
				int xPos = margin + 1;
				
				// if new section, indent 2 blank word
				if (newSection) {
					xPos += ((wordWidth + space) * 2);
					wordsNumber -= 2;
					
					newSection = false;
				}
				
				for (int j = 0; j < wordsNumber; j++) {
					if (words.isEmpty()) {
						break;
					}
					
					//if section end
					word = words.remove(0);
					if (word[0] == -1) {
						newSection = true;
						break;
					}
					
					//crop word image and draw on page
					BufferedImage wordImage = image.getSubimage(word[1], word[0], word[3] - word[1] + 1, word[2] - word[0] + 1);
					graphics.drawImage(wordImage, null, xPos, yPos);
					xPos += wordWidth;
					xPos += space;
				}
				
				yPos += wordHeight;
				yPos += space;
			}
			
			ImageIO.write(pageImage, "png", new File(pdfDirPath + "/" + pageNumber + ".png"));
			pageNumber++;
		}
		
		return pageNumber;
	}
	
	private static int[] wordRect(BufferedImage image) {
		int[] line = getLineOfWords(image, 0);
		int[] column = getColumnOfWords(image, 0);
		
		return new int[] {line[0], column[0], line[1], column[1]};
	}
	
	private static ArrayList<int[]> wordsInImage(BufferedImage image, int wordHeight) {
		ArrayList<int[]> lines = new ArrayList<int[]>();
		ArrayList<int[]> columns = new ArrayList<int[]>();
		
		int yOffset = 0;
		while (yOffset < image.getHeight()) {
			int[] line = getLineOfWords(image, yOffset);
			if (line[0] == -1) {
				//no more line
				break;
			}
			
			lines.add(line);
			yOffset = line[1] + 1;
		}
		
		int xOffset = 0;
		while (true) {
			int[] column = getColumnOfWords(image, xOffset);
			if (column[0] == -1) {
				//no more column
				break;
			}
			
			columns.add(column);
			xOffset = column[1] + 1;
		}
		
		ArrayList<int[]> words = new ArrayList<int[]>();
		for (int i = 0; i < lines.size(); i++) {
			int[] line = lines.get(i);
			
			if (i >= 1) {
				int[] prevLine = lines.get(i - 1);
				if (line[0] - prevLine[1] > wordHeight) {
					words.add(new int[] {-1, -1, -1, -1}); //section flag
				}
			}
			
			for (int j = 0; j < columns.size(); j++) {
				int[] column = columns.get(j);
				
				int[] word = new int[] {line[0], column[0], line[1], column[1]};
				if (!isBlankWord(image, word)) {
					words.add(word);
				}
			}
		}
		return words;
	}
	
	private static int[] getLineOfWords(BufferedImage image, int pos) {
		int start = -1;
		int end = -1;
		
		for (int i = pos; i < image.getHeight(); i++) {
			boolean isBackground = true;
			for (int j = 0; j < image.getWidth(); j++) {
				int gray = image.getRGB(j, i) & 0xff;
				//FIXME: background gray is bigger than 240
				if (gray < 240) {
					isBackground = false;
					break;
				}
			}
			
			if (isBackground) {
				if (start != -1) {
					break;
				}
			}
			else {
				if (start == -1) {
					start = i;
					end = start;
				}
				else {
					end += 1;
				}
			}
		}
		
		return new int[] {start, end};
	}
	
	private static int[] getColumnOfWords(BufferedImage image, int pos) {
	    int start = -1;
	    int end = -1;

	    for (int i = pos; i < image.getWidth(); i++) {
	        boolean isBackground = true;
	        for (int j = 0; j < image.getHeight(); j++) {
				int gray = image.getRGB(i, j) & 0xff;
				//FIXME: background gray is bigger than 240
				if (gray < 240) {
					isBackground = false;
					break;
				}
			}
	        
	        if (isBackground) {
				if (start != -1) {
					break;
				}
			}
			else {
				if (start == -1) {
					start = i;
					end = start;
				}
				else {
					end += 1;
				}
			}
	    }
	    
	    return new int[] {start, end};
	}
	
	private static boolean isBlankWord(BufferedImage image, int[] word) {
		for (int i = word[0]; i <= word[2]; i++) {
			for (int j = word[1]; j <= word[3]; j++) {
				int gray = image.getRGB(j, i) & 0xff;
				//FIXME: background gray is bigger than 240
				if (gray < 240) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	private static void parseEpisodePage(String episodePageURL, final String episodeDirPath) throws ParserException, IOException {
		Parser parser = new Parser(createConnection(new URL(episodePageURL), true));
		
		//find plot information
		NodeList plotNodes = parser.parse(null)
				.extractAllNodesThatMatch(new TagNameFilter("img"), true);
		for (int i = 0; i < plotNodes.size(); i++) {
			Node plotNode = plotNodes.elementAt(i);
			
			plotNode.accept(new NodeVisitor() {
				public void visitTag (Tag tag) {
					String width = tag.getAttribute("width");
					if ((width != null) && width.equals("760")) {
						String plotImageSrc = tag.getAttribute("src");
						if (plotImageSrc == null) {
							System.out.print("plot is writing now, skip" + "\r\n");
							return;
						}
						//TODO: a BUG in htmlparser. correct source is "https" but here we get "http"
						if (plotImageSrc.contains("livefilestore.com")) {
							plotImageSrc = plotImageSrc.replace("http", "https");
						}
						
						int pos = plotImageSrc.lastIndexOf("/");
						if (pos < 0) {
							System.out.print("plot is writing now, skip" + "\r\n");
							return;
						}
						String imageName = plotImageSrc.substring(pos + 1);
						String imageURL = plotImageSrc;
						
						File imageFile = new File(episodeDirPath + "/" + imageName);
						if (!imageFile.exists()) {
							System.out.print("start downloading " + imageName + "\r\n");
							
							boolean retry = false;
							do {
								try {
									downloadPlotImage(imageURL, imageFile.getPath());
									retry = imageFile.length() == 0;
								}
								catch (IOException e) {
									System.err.print("download plot image error, exception: " + e.getMessage() + "\r\n");
									retry = true;
								}
								
								if (retry) {
									System.out.print("download plot image fail, retry after 5s\r\n");
									
									try {
										Thread.sleep(5000);
									} 
									catch (InterruptedException e1) {
									}
								}
							}
							while (retry);
						}
					}
				}
			});
		}
	}
	
	private static void downloadPlotImage(String imageURL, String imageFilePath) throws IOException {
		InputStream in = openImage(imageURL);
		OutputStream out = new FileOutputStream(imageFilePath);
		
		byte[] buf = new byte[1024];
		while (true) {
			int ret = in.read(buf);
			if (ret < 0) {
				break;
			}
			
			out.write(buf, 0, ret);
		}
		
		in.close();
		out.close();
	}
	
	private static InputStream openImage(String imageURL) throws IOException {
		URLConnection conn = createConnection(new URL(imageURL), true);
		conn.setConnectTimeout(2000);
		
		return conn.getInputStream();
	}
	
	private static URLConnection createConnection(URL url, boolean proxy) throws IOException {
		if (!proxy) {
			return url.openConnection();
		}
		else {
			//FIXME: local lantern HTTP proxy
			return url.openConnection(createProxy("127.0.0.1", 37689));
		}
	}
	
	private static Proxy createProxy(String ip, int port) {
		return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
	}
}
