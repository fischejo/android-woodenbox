package de.pecheur.colorbox.port;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.pecheur.colorbox.models.Unit;
import de.pecheur.colorbox.models.Word;

class AsyncZipLoader extends AsyncTaskLoader<List<UnitMap>> {
	
	
	private Uri mUri;
	private Unit mUnit;
    private File cache;


	public AsyncZipLoader(Context context, Uri uri) {
		super(context);
		mUri = uri;
		mUnit = null;
        cache = context.getExternalCacheDir();
	}

	@Override
	public List<UnitMap> loadInBackground() {
        try {

            InputStream is = getContext().getContentResolver().openInputStream(mUri);
            // SAXParser closes this stream, before all entries are unzipped.
            // ModZipInputStream only get closed with reallyClose().
            ModZipInputStream zip = new ModZipInputStream(is);

            ZipEntry entry = null;
            List<UnitMap> content = new ArrayList<UnitMap>();

            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().equals("content.xml")) {
                    // try to parse content
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser parser = factory.newSAXParser();
                    parser.parse(zip, new XmlHandler(content));
                } else {
                    File f = new File(cache, entry.getName());
                    FileOutputStream fos = new FileOutputStream(f);
                    copy(zip, fos);
                    fos.close();
                }
                //zip.closeEntry();
            }
            zip.reallyClose();

            return content;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

	@Override
	protected void onStartLoading() {
		forceLoad();
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

	@Override
	protected void onReset() {
		super.onReset();
		onStopLoading();
	}

	private class XmlHandler extends DefaultHandler {
        private Word word;
        private StringBuilder builder;
        private List<UnitMap> content;
        private UnitMap unit;


        public XmlHandler(List<UnitMap> content) {
            this.content = content;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            builder = new StringBuilder();
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            if (localName.equalsIgnoreCase(Xml.WORD)) {
                // word
                word = new Word(unit);
                unit.add(word);

            } else if (localName.equalsIgnoreCase(Xml.UNIT)) {
                // unit
                unit = new UnitMap();
                unit.setFrontCode(attributes.getValue(Xml.FRONT));
                unit.setBackCode(attributes.getValue(Xml.BACK));
                unit.setTitle(attributes.getValue(Xml.TITLE));

                // add unit and a new word list to content map
                content.add(unit);
            } else if (localName.equalsIgnoreCase(Xml.FRONT)) {
                //word.setSide(true);
            } else if (localName.equalsIgnoreCase(Xml.BACK)) {
                //word.setSide(false);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            builder.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            if (word != null) {
                // text
                if (localName.equalsIgnoreCase(Xml.TEXT)) {
                    //word.setText(builder.toString());

                }

                // audio
                if (localName.equalsIgnoreCase(Xml.AUDIO)) {
                    File file = new File(cache, builder.toString());
//                    word.setAudio(Uri.fromFile(file));
                }

                // reset string builder
                builder.setLength(0);
            }
        }
    }


    private int copy(InputStream in, OutputStream out) throws IOException {
        int count = 0;
        int read = 0;
        byte[] buffer = new byte[1024];
        while (read != -1) {
            read = in.read(buffer);
            if (read != -1) {
                count += read;
                out.write(buffer,0,read);
            }
        }
        return count;
    }

    private class ModZipInputStream extends ZipInputStream {
        public ModZipInputStream(InputStream stream) {
            super(stream);
        }

        public void close () {
            // Do nothing.
        }

        public void reallyClose() throws IOException {
            super.close ();
        }
    };
}