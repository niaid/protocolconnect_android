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
 * Adapter for ListView for Contact list.
 */
public class ContactListAdapter extends ArrayAdapter<Contact> {

    private Context context;
    private List<Contact> allContacts;

    public ContactListAdapter(Context context, List<Contact> allContacts) {
        super(context, R.layout.contact_item, allContacts);
        this.context = context;
        this.allContacts = new ArrayList<Contact>();
        this.allContacts = allContacts;
    }

    private static class ViewHolder {
        private TextView name;
        private TextView email;
        private TextView number;
    }

    @Override
    public int getCount() {
        return this.allContacts.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder = null;

        if (convertView == null) {
            mViewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.contact_item, parent, false);
            mViewHolder.name = (TextView) convertView.findViewById(R.id.contact_name);
            mViewHolder.email = (TextView) convertView.findViewById(R.id.contact_email);
            mViewHolder.number = (TextView) convertView.findViewById(R.id.contact_number);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        Contact oneContact = allContacts.get(position);
        mViewHolder.name.setText(oneContact.getContactName());
        mViewHolder.email.setText(oneContact.getEmail());
        mViewHolder.number.setText(oneContact.getPhoneNumber());

        return convertView;
    }
}