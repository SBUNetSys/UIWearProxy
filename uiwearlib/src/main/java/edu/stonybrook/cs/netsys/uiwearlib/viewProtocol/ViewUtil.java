package edu.stonybrook.cs.netsys.uiwearlib.viewProtocol;

import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.CLICK_ID_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.CLICK_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.PKG_KEY;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.orhanobut.logger.Logger;

import java.io.File;

import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataNode;

/**
 * Created by qqcao on 11/23/16Wednesday.
 *
 * View related actions
 */

public class ViewUtil {
    public static void setViewListener(DataNode node, View nodeView) {
        final int clickId = node.getClickId();
        final Context context = nodeView.getContext();
        nodeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent clickIntent = new Intent(CLICK_PATH);
                clickIntent.putExtra(PKG_KEY, context.getPackageName());
                clickIntent.putExtra(CLICK_ID_KEY, clickId);
                context.sendBroadcast(clickIntent);
            }
        });
    }

    public static void renderView(Context context, DataNode node, View nodeView) {
        Logger.d("render node: " + node);
        Logger.d("render view: " + nodeView.toString());
        // use mapping rule field to set text and image info
        if (hasTextInfo(node)) {
            if (nodeView instanceof TextView) {
                ((TextView) nodeView).setText(node.getText());
            }
        }

        if (hasImageInfo(node)) {
            File imageFile = new File(node.getImageFile());
//            Uri imageUri = Uri.parse(node.getImageFile());
            Logger.v("image file: " + imageFile);
            Glide.with(context).load(imageFile).asBitmap().into(getViewTarget(context, nodeView));
        }
    }

    private static SimpleTarget<Bitmap> getViewTarget(final Context context, final View nodeView) {
        return new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource,
                    GlideAnimation<? super Bitmap> glideAnimation) {
                Drawable drawable = new BitmapDrawable(context.getResources(), resource);
                nodeView.setBackground(drawable);
            }
        };
    }

    private static boolean hasTextInfo(DataNode node) {
        return node.getText() != null && !node.getText().isEmpty();
    }

    private static boolean hasImageInfo(DataNode node) {
        return node.getImageFile() != null && !node.getImageFile().isEmpty();
    }
}
