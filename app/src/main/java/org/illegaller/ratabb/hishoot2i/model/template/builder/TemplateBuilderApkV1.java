package org.illegaller.ratabb.hishoot2i.model.template.builder;

import com.nostra13.universalimageloader.utils.L;

import org.illegaller.ratabb.hishoot2i.di.ir.UserDeviceDensity;
import org.illegaller.ratabb.hishoot2i.model.Sizes;
import org.illegaller.ratabb.hishoot2i.model.pref.IntPreference;
import org.illegaller.ratabb.hishoot2i.model.template.TemplateType;
import org.illegaller.ratabb.hishoot2i.utils.UILHelper;
import org.illegaller.ratabb.hishoot2i.utils.Utils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.Display;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;


public class TemplateBuilderApkV1 extends AbstractTemplateBuilder {
    @Inject @UserDeviceDensity IntPreference userDensity;

    public TemplateBuilderApkV1(Context context, String packageName) {
        super(context);
        id = packageName;
        type = TemplateType.APK_V1;
        TemplateReader reader;
        InputStream inputStream = null;
        try {
            inputStream = Utils.getAssetsStream(context, packageName, "keterangan.xml");
            reader = new TemplateReader(inputStream);
            templateSizes = Utils.getSizesBitmapTemplate(context, packageName, "skin");
            previewFile = frameFile = UILHelper.stringTemplateApp(packageName,
                    Utils.getResIdDrawableTemplate(context, packageName, "skin"));
            name = reader.device;
            author = reader.author;
            offset = Sizes.create(reader.tx, reader.ty);
            screenSizes = Sizes.create(templateSizes.width - (reader.tx + reader.bx),
                    templateSizes.height - (reader.ty + reader.by));

            isCompatible = screenSizes.equals(userDeviceScreenSizes);
            // FIXME
            /**  {@linkplain Utils#getDensity(Display)}  */
            isCompatible |= reader.densType == userDensity.get();
        } catch (PackageManager.NameNotFoundException | XmlPullParserException | IOException e) {
            String msg = "Template: " + packageName + " can't load";
            L.e(e, msg);
        } finally {
            Utils.tryClose(inputStream);
        }
    }


    class TemplateReader {
        String device = null;
        String author = null;
        int tx, ty, bx, by, densType;

        public TemplateReader(InputStream inputStream) throws IOException, XmlPullParserException {
            String value = null;
            XmlPullParserFactory factory;
            XmlPullParser xpp;
            // try-catch here ?
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();

            xpp.setInput(inputStream, null);

            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String xppName = xpp.getName();

                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT: // no-op
                        break;
                    case XmlPullParser.TEXT:
                        value = xpp.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if (null == value) continue;
                        if (xppName.equalsIgnoreCase("device")) {
                            this.device = value;
                        } else if (xppName.equalsIgnoreCase("author")) {
                            this.author = value;
                        } else if (xppName.equalsIgnoreCase("topx")) {
                            this.tx = Integer.parseInt(value);
                        } else if (xppName.equalsIgnoreCase("topy")) {
                            this.ty = Integer.parseInt(value);
                        } else if (xppName.equalsIgnoreCase("botx")) {
                            this.bx = Integer.parseInt(value);
                        } else if (xppName.equalsIgnoreCase("boty")) {
                            this.by = Integer.parseInt(value);
                        } else if (xppName.equalsIgnoreCase("deviceDpi")) {
                            this.densType = Integer.parseInt(value);
                        }
                        break;
                    default:
                        break;
                }
                eventType = xpp.nextToken();
            }
            //end try-catch

        }

    }

}