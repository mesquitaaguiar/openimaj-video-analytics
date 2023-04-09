package br.com.cameag.processing.main;

import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.alignment.AffineAligner;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.keypoints.FKEFaceDetector;
import org.openimaj.image.processing.face.detection.keypoints.KEDetectedFace;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

import Jama.Matrix;

public class Mustache {
	MBFImage mustache;
	private FKEFaceDetector detector;

	public static class VideoMustache {
		private Mustache m = new Mustache();

		public VideoMustache() throws IOException {
			final VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(new VideoCapture(640, 350));

			vd.addVideoListener(new VideoDisplayListener<MBFImage>()
			{
				@Override
				public void beforeUpdate(MBFImage frame)
				{
					frame.internalAssign(m.addMustaches(frame));
				}

				@Override
				public void afterUpdate(VideoDisplay<MBFImage> display)
				{
				}
			});
		}
	}

	public Mustache() throws IOException {
		this(ImageUtilities.readMBFAlpha(Mustache.class.getResourceAsStream("/org/openimaj/demos/faces/mustache.png")));
	}

	public Mustache(MBFImage mustache) {
		this.mustache = mustache;
		this.detector = new FKEFaceDetector(new HaarCascadeDetector(80));
	}

	public MBFImage addMustaches(MBFImage image) {
		MBFImage cimg;

		if (image.getWidth() > image.getHeight() && image.getWidth() > 640) {
			cimg = image.process(new ResizeProcessor(640, 480));
		} else if (image.getHeight() > image.getWidth() && image.getHeight() > 640) {
			cimg = image.process(new ResizeProcessor(480, 640));
		} else {
			cimg = image.clone();
		}
		
		final FImage img = Transforms.calculateIntensityNTSC(cimg);

		final List<KEDetectedFace> faces = detector.detectFaces(img);
		final MBFImageRenderer renderer = cimg.createRenderer();

		for (final KEDetectedFace face : faces) {
			final Matrix tf = AffineAligner.estimateAffineTransform(face);
			final Shape bounds = face.getBounds();

			final MBFImage m = mustache.transform(tf.times(TransformUtilities.scaleMatrix(1f / 4f, 1f / 4f)));

			renderer.drawImage(m, (int) bounds.minX(), (int) bounds.minY());
		}

		return cimg;
	}

	public static void main(String[] args) throws IOException {
		args = new String[] { "-v" };
		if (args.length > 0 && args[0].equals("-v")) {
			new Mustache.VideoMustache();
		} else {
			MBFImage cimg = ImageUtilities.readMBF(Mustache.class
					.getResourceAsStream("/org/openimaj/demos/image/sinaface.jpg"));

			cimg = new Mustache().addMustaches(cimg);

			DisplayUtilities.display(cimg);
		}
	}
}
