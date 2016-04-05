package com.suda.jzapp.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.suda.jzapp.R;
import com.suda.jzapp.dao.bean.RecordDetailDO;
import com.suda.jzapp.dao.greendao.RecordDao;
import com.suda.jzapp.dao.local.record.RecordLocalDAO;
import com.suda.jzapp.manager.RecordManager;
import com.suda.jzapp.misc.IntentConstant;
import com.suda.jzapp.ui.activity.record.CreateOrEditRecordActivity;
import com.suda.jzapp.util.IconTypeUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ghbha on 2016/4/5.
 */
public class RecordAdapter extends BaseAdapter {

    private Context mContext;
    private List<RecordDetailDO> recordDetailDOs;
    private LayoutInflater mInflater;
    private List<View> optViews;
    private int lastSelOpt = -1;
    private RecordLocalDAO recordLocalDAO;

    public RecordAdapter(Context context, List<RecordDetailDO> list) {
        recordDetailDOs = list;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        optViews = new ArrayList<>();
        recordLocalDAO = new RecordLocalDAO();
    }

    @Override
    public int getCount() {
        return recordDetailDOs.size();
    }

    @Override
    public Object getItem(int position) {
        return recordDetailDOs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.record_item, null);
            holder.icon = (ImageView) convertView.findViewById(R.id.record_icon);
            holder.inLy = convertView.findViewById(R.id.shouru_ly);
            holder.outLY = convertView.findViewById(R.id.zhichu_ly);
            holder.outTv = (TextView) convertView.findViewById(R.id.out_tv);
            holder.inTv = (TextView) convertView.findViewById(R.id.in_tv);
            holder.outRemarkTv = (TextView) convertView.findViewById(R.id.out_remark_tv);
            holder.inRemarkTv = (TextView) convertView.findViewById(R.id.in_remark_tv);
            holder.recordDateTv = (TextView) convertView.findViewById(R.id.record_date);
            holder.delV = convertView.findViewById(R.id.icon_del);
            holder.editV = convertView.findViewById(R.id.icon_edit);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final RecordDetailDO recordDetailDO = recordDetailDOs.get(position);

        boolean isFirst = false;
        if (getPositionForSection(recordDetailDO.getRecordDate().getTime()) == position) {
            isFirst = true;
        }

        holder.inLy.setVisibility(recordDetailDO.getRecordMoney() > 0 || (isFirst && recordDetailDO.getTodayAllInMoney() > 0)
                ? View.VISIBLE : View.INVISIBLE);
        holder.outLY.setVisibility(recordDetailDO.getRecordMoney() < 0 || (isFirst && recordDetailDO.getTodayAllOutMoney() < 0)
                ? View.VISIBLE : View.INVISIBLE);

        holder.icon.setVisibility(isFirst ? View.INVISIBLE : View.VISIBLE);
        holder.recordDateTv.setVisibility(isFirst ? View.VISIBLE : View.INVISIBLE);

        resetOptBt();

        if (recordDetailDO.getRecordMoney() < 0) {
            holder.outTv.setText(recordDetailDO.getRecordDesc() + " " +
                    String.format(mContext.getResources().getString(R.string.record_money_format), Math.abs(recordDetailDO.getRecordMoney())));
            holder.outRemarkTv.setText(recordDetailDO.getRemark());
        }

        if (recordDetailDO.getRecordMoney() > 0) {
            holder.inTv.setText(recordDetailDO.getRecordDesc() + " " +
                    String.format(mContext.getResources().getString(R.string.record_money_format), Math.abs(recordDetailDO.getRecordMoney())));
            holder.inRemarkTv.setText("");
        }

        if (recordDetailDO.getTodayAllInMoney() >= 0 && isFirst) {
            holder.inTv.setText(String.format(mContext.getResources().getString(R.string.record_money_format), Math.abs(recordDetailDO.getTodayAllInMoney()))
                    + " 收入");
            holder.outRemarkTv.setText("");
        }

        if (recordDetailDO.getTodayAllOutMoney() <= 0 && isFirst) {
            holder.outTv.setText("支出 " + String.format(mContext.getResources().getString(R.string.record_money_format), Math.abs(recordDetailDO.getTodayAllOutMoney())));
            holder.inRemarkTv.setText("");
        }

        DateFormat format = new SimpleDateFormat("dd");
        holder.recordDateTv.setText(format.format(recordDetailDO.getRecordDate()) + "日");
        holder.icon.setImageResource(IconTypeUtil.getTypeIcon(recordDetailDO.getIconId()));

        final ViewHolder finalHolder = holder;

        if (!isFirst) {
            holder.icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetOptBt();
                    if (lastSelOpt != position) {
                        optViews.add(finalHolder.delV);
                        optViews.add(finalHolder.editV);
                        finalHolder.delV.setVisibility(View.VISIBLE);
                        finalHolder.editV.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInUp).duration(200).playOn(finalHolder.delV);
                        YoYo.with(Techniques.SlideInDown).duration(200).playOn(finalHolder.editV);
                        lastSelOpt = position;
                    } else {
                        lastSelOpt = -1;
                    }
                }
            });
        }

        holder.delV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.editV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CreateOrEditRecordActivity.class);
                intent.putExtra(IntentConstant.OLD_RECORD, recordLocalDAO.getRecordById(mContext, recordDetailDO.getRecordID()));
                mContext.startActivity(intent);
            }
        });

        return convertView;
    }

    private void resetOptBt() {
        for (View view : optViews) {
            view.setVisibility(View.INVISIBLE);
        }
    }

    private int getPositionForSection(long sectionIndex) {
        if (recordDetailDOs != null && recordDetailDOs.size() > 0) {
            for (int i = 0; i < recordDetailDOs.size(); i++) {
                if (recordDetailDOs.get(i).getRecordDate().getTime() == sectionIndex)
                    return i;
            }
        }
        return 0;
    }


    public class ViewHolder {
        public View outLY, inLy;
        public TextView outTv, inTv, outRemarkTv, inRemarkTv, recordDateTv;
        public ImageView icon;
        public View delV, editV;
    }
}