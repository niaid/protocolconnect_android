package niaid.lsb.protocolconnect.classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import niaid.lsb.protocolconnect.R;

/**
 * Adapter for ListView for Messages list.
 */
public class MessageListAdapter extends ArrayAdapter<Message> {

    private static final int TYPE_TO_SUBJECT = 0;
    private static final int TYPE_FROM_SUBJECT = 1;
    private static final int TYPE_MAX_COUNT = 2;

    private Context context;
    private List<Message> allMessages;

    public MessageListAdapter(Context context, int resource, List<Message> allMessages) {
        super(context, resource, allMessages);
        this.context = context;
        this.allMessages = new ArrayList<>();
        this.allMessages = allMessages;
    }

    private static class ViewHolder {
        private TextView date;
        private TextView message;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (this.allMessages.get(position).getIsToSubject()) {
            return TYPE_TO_SUBJECT;
        } else {
            return TYPE_FROM_SUBJECT;
        }
    }

    @Override
    public int getCount() {
        return this.allMessages.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder = null;
        Message oneMessage = allMessages.get(position);
        int listViewItemType = getItemViewType(position);

        if (convertView == null) {
            mViewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (listViewItemType == TYPE_TO_SUBJECT) {
                convertView = inflater.inflate(R.layout.message_item2, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.message_item1, parent, false);
            }
            mViewHolder.date = (TextView) convertView.findViewById(R.id.message_date);
            mViewHolder.message = (TextView) convertView.findViewById(R.id.message_text);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        mViewHolder.date.setText(oneMessage.printDate());
        mViewHolder.message.setText(oneMessage.getMessage());

        return convertView;
    }
}