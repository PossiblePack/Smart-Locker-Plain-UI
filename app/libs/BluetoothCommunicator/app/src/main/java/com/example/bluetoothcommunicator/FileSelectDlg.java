package com.example.bluetoothcommunicator;

import android.app.AlertDialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class FileSelectDlg implements AdapterView.OnItemClickListener {
    static public class FileInfo implements Comparable<FileInfo>
    {
        private String m_strName;
        private File m_file;

        public FileInfo( String strName, File file )
        {
            m_strName = strName;
            m_file = file;
        }

        public String getName()
        {
            return m_strName;
        }

        public File getFile()
        {
            return m_file;
        }

        public int compareTo( FileInfo another )
        {
            if( m_file.isDirectory() && !another.getFile().isDirectory() )
            {
                return -1;
            }
            if( !m_file.isDirectory() && another.getFile().isDirectory() )
            {
                return 1;
            }

            return m_file.getName().toLowerCase().compareTo( another.getFile().getName().toLowerCase() );
        }
    }

    static public class FileInfoArrayAdapter extends BaseAdapter
    {
        private Context m_context;
        private List<FileInfo> m_listFileInfo;

        public FileInfoArrayAdapter( Context context, List<FileInfo> list )
        {
            super();
            m_context = context;
            m_listFileInfo = list;
        }

        @Override
        public int getCount()
        {
            return m_listFileInfo.size();
        }

        @Override
        public FileInfo getItem( int position )
        {
            return m_listFileInfo.get( position );
        }

        @Override
        public long getItemId( int position )
        {
            return position;
        }

        static class ViewHolder
        {
            TextView textviewFileName;
            TextView textviewFileSize;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent )
        {
            ViewHolder viewHolder;
            if( null == convertView )
            {
                LinearLayout layout = new LinearLayout( m_context );
                layout.setOrientation( LinearLayout.VERTICAL );
                layout.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT ) );
                TextView textviewFileName = new TextView( m_context );
                textviewFileName.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 24 );
                layout.addView( textviewFileName, new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT ) );
                TextView textviewFileSize = new TextView( m_context );
                textviewFileSize.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 12 );
                layout.addView( textviewFileSize, new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT ) );

                convertView = layout;
                viewHolder = new ViewHolder();
                viewHolder.textviewFileName = textviewFileName;
                viewHolder.textviewFileSize = textviewFileSize;
                convertView.setTag( viewHolder );
            }
            else
            {
                viewHolder = (ViewHolder)convertView.getTag();
            }

            FileInfo fileinfo = m_listFileInfo.get( position );
            if( fileinfo.getFile().isDirectory() )
            {
                viewHolder.textviewFileName.setText( fileinfo.getName() + "/" );
                viewHolder.textviewFileSize.setText( "(directory)" );
            }
            else
            {
                viewHolder.textviewFileName.setText( fileinfo.getName() );
                viewHolder.textviewFileSize.setText( String.valueOf( fileinfo.getFile().length() / 1024 ) + " [KB]" );
            }

            return convertView;
        }
    }

    private Context              m_contextParent;
    private OnFileSelectListener m_listener;
    private AlertDialog m_dialog;
    private FileInfoArrayAdapter m_fileinfoarrayadapter;
    private String[]				m_astrExt;

    public FileSelectDlg( Context context, OnFileSelectListener listener, String strExt )
    {
        m_contextParent = context;
        m_listener = (OnFileSelectListener) listener;

        // 拡張子フィルタ
        if( null != strExt )
        {
            StringTokenizer tokenizer = new StringTokenizer( strExt, "; " );
            int iCountToken = 0;
            while( tokenizer.hasMoreTokens() )
            {
                tokenizer.nextToken();
                iCountToken++;
            }
            if( 0 != iCountToken )
            {
                m_astrExt = new String[iCountToken];
                tokenizer = new StringTokenizer( strExt, "; " );
                iCountToken = 0;
                while( tokenizer.hasMoreTokens() )
                {
                    m_astrExt[iCountToken] = tokenizer.nextToken();
                    iCountToken++;
                }
            }
        }
    }

    public void show( File fileDirectory )
    {
        String strTitle = fileDirectory.getAbsolutePath();

        ListView listview = new ListView( m_contextParent );
        listview.setScrollingCacheEnabled( false );
        listview.setOnItemClickListener( this );
        File[]         aFile        = fileDirectory.listFiles(getFileFilter());
        List<FileInfo> listFileInfo = new ArrayList<>();
        if( null != aFile )
        {
            for( File fileTemp : aFile )
            {
                listFileInfo.add( new FileInfo( fileTemp.getName(), fileTemp ) );
            }
            Collections.sort( listFileInfo );
        }
        if( null != fileDirectory.getParent() )
        {
            listFileInfo.add( 0, new FileInfo( "..", new File( fileDirectory.getParent() ) ) );
        }
        m_fileinfoarrayadapter = new FileInfoArrayAdapter( m_contextParent, listFileInfo );
        listview.setAdapter( m_fileinfoarrayadapter );

        AlertDialog.Builder builder = new AlertDialog.Builder( m_contextParent );
        builder.setTitle( strTitle );
        builder.setNegativeButton( "Cancel", null );
        builder.setView( listview );
        m_dialog = builder.show();
    }

    private FileFilter getFileFilter()
    {
        return new FileFilter()
        {
            public boolean accept( File arg0 )
            {
                if( null == m_astrExt )
                { // フィルタしない
                    return true;
                }
                if( arg0.isDirectory() )
                { // ディレクトリのときは、true
                    return true;
                }
                for( String strTemp : m_astrExt )
                {
                    if( arg0.getName().toLowerCase().endsWith( "." + strTemp ) )
                    {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id )
    {
        if( null != m_dialog )
        {
            m_dialog.dismiss();
            m_dialog = null;
        }

        FileInfo fileinfo = m_fileinfoarrayadapter.getItem( position );

        if( fileinfo.getFile().isDirectory() )
        {
            show( fileinfo.getFile() );
        }
        else
        {
            m_listener.onFileSelect( fileinfo.getFile() );
        }
    }

    public interface OnFileSelectListener
    {
        void onFileSelect( File file );
    }
}
