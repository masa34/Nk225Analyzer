package com.masa34.nk225analyzer.Stock;

import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

//import static android.R.attr.y;

public class MarketCalendar {

    public static boolean isMarketHoliday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
            case Calendar.SATURDAY:
                // 土日は休業日
                return true;

            default:
                // 年始3日間及び12月31日は休業日
                int m = cal.get(Calendar.MONTH) + 1;
                int d = cal.get(Calendar.DAY_OF_MONTH);
                if (m == 1 && d == 1) return true;
                if (m == 1 && d == 2) return true;
                if (m == 1 && d == 3) return true;
                if (m == 12 && d == 31) return true;

                // 祝日は休業日
                int[] days = Holiday.listHoliDays(cal.get(Calendar.YEAR), m - 1);
                if (Arrays.binarySearch(days, d) >= 0) {
                    return true;
                }
                break;
        }

        return false;
    }

    public static Date getLastBussinessDay(Date date) {
        Date lastDate;

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        while (true) {
            cal.add(Calendar.DATE, -1);
            lastDate = cal.getTime();
            if (!isMarketHoliday(lastDate)) {
                break;
            }
        }

        return lastDate;
    }

    public static Date getNextBussinessDay(Date date) {
        Date nextDate;

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        while (true) {
            cal.add(Calendar.DATE, 1);
            nextDate = cal.getTime();
            if (!isMarketHoliday(nextDate)) {
                break;
            }
        }

        return nextDate;
    }

    /**
     * 祝日計算クラス.
     * <pre>
     * 2007年以降のみ有効
     * 春分・秋分の日は、『海上保安庁水路部 暦計算研究会編 新こよみ便利帳』による計算式に
     * よる結果を求めてるにすぎない。毎年の官報公示の決定と異なったら官報公示に従うこと。
     * 年間の祝日リスト(国民の休日を含む）（配列）の算出、
     * 年と月を指定して対象月の祝日(国民の休日を含む）の算出、
     * 指定日付の祝日判定、
     * 指定する祝日の日付の取得を目的とする。
     *
     * 2016年以降、８月１１日  を「山の日」とする。改正祝日法、2014-5-23 参議院本会議で可決し成立した。
     *
     * 任意の祝日を指定して情報を取得するために、
     *     public abstract class HolidayBundle を提供している。
     * この抽象クラスの具象クラスとして祝日ごとのクラスが用意されており、祝日名、祝日計算は
     * 個々の祝日HolidayBundle で実装する。
     * 振替休日計算は、HolidayBundle 抽象クラスで実装するが、特定の祝日は具象クラスで
     * オーバーライドで実装する。
     * 【使用例】
     *     // 2009年の祝日配列
     *         Holiday holday = new Holiday(2009);  // コンストラクタを使用すること
     *         Date[] ary = holday.listHoliDays();
     *         for(int i=0;i < ary.length;i++){
     *            System.out.println(Holiday.YMD_FORMAT.format(ary[i])
     *                          +"\t"+Holiday.dateOfWeekJA(ary[i])
     *                          +"\t"+Holiday.queryHoliday(ary[i]));
     *         }
     *     // 2009年の９月の祝日、
     *         int[] days = Holiday.listHoliDays(2009,Calendar.SEPTEMBER);
     *         Date[] dts = Holiday.listHoliDayDates(2009,Calendar.SEPTEMBER);
     *     // 指定日付の祝日判定
     *        String target = "2009/05/06";
     *        String res = Holiday.queryHoliday(Holiday.YMD_FORMAT.parse(target));
     *        System.out.println(res);
     *     // 指定する祝日の日付の取得
     *     //   Holiday.HolidayType enum から、getBundleメソッドで、Holiday.HolidayBundle
     *     //   を取得してHoliday.HolidayBundleが提供するメソッドを利用する
     *          //    Holiday.HolidayBundle#getMonth()       → 月
     *          //    Holiday.HolidayBundle#getDay()         → 日
     *          //    Holiday.HolidayBundle#getDescription() → 祝日名
     *          //    Holiday.HolidayBundle#getDate()        → 祝日のDate
     *          //    Holiday.HolidayBundle#getChangeDay()   → 振替休日ある場合のDay
     *          //    Holiday.HolidayBundle#getChangeDate()  → 振替休日ある場合のDate
     *       // 2009年の春分の日
     *          Holiday.HolidayBundle h = Holiday.HolidayType.SPRING_EQUINOX_DAY.getBundle(2009);
     *          System.out.println(h.getMonth()+"月 "+h.getDay()+"日"
     *                              +"（"+Holiday.WEEKDAYS_JA[h.getWeekDay()-1]+"）"
     *                              +" "+h.getDescription());
     *     // 指定年→国民の休日のみのDate[]の取得
     *          // 2009年の国民の休日配列
     *          Date[] ds = Holiday.getNatinalHoliday(2009);
     *          for(int i=0;i < ds.length;i++){
     *             System.out.println(Holiday.YMD_FORMAT.format(ds[i])+"-->"+Holiday.queryHoliday(ds[i]));
     *          }
     * </pre>
     */
    private static class Holiday{
        private Date[] holidayDates;
        /**
         * デフォルトコンストラクタ.
         * 現在日の年で、Holiday(int year) コンストラクタを呼び出すのと同じ効果です。
         */
        public Holiday(){
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            this.init(cal.get(Calendar.YEAR));
        }
        /**
         * 対象年 指定コンストラクタ.
         * @param year 西暦４桁
         */
        public Holiday(int year){
            this.init(year);
        }
        private void init(int year){
            TreeSet<Date> set = new TreeSet<Date>();
            HolidayType[] holidayTypes = HolidayType.values();
            for(int i=0;i < holidayTypes.length;i++){
                HolidayBundle hb = holidayTypes[i].getBundle(year);
                if (hb != null){
                    if (year < 2016 && hb.getMonth()==8) continue;  // since ver 2.0
                    set.add(hb.getDate());
                    Date chgdt = hb.getChangeDate();
                    if (chgdt != null){
                        set.add(chgdt);
                    }
                }
            }
            Date[] ds = getNatinalHoliday(year);
            if (ds.length > 0){
                for(Date d:ds) set.add(d);
            }
            if (year == 2019){
                set.add(toDate(2019, 4, 30));
                set.add(toDate(2019, 5, 1));
                set.add(toDate(2019, 5, 2));
                set.add(toDate(2019, 10, 22));
            }
            this.holidayDates = new Date[set.size()];
            int n=0;
            for(Iterator<Date> it=set.iterator();it.hasNext();n++){
                this.holidayDates[n] = it.next();
            }
        }
        private static Date toDate(int year, int month, int day){
            Calendar cal = Calendar.getInstance();
            cal.set(year, month - 1, day);
            return cal.getTime();
        }
        /** HolidayType は、祝日タイプ→HolidayBundle class を紐付ける enum */
        public enum HolidayType{
            /** 元旦        ：１月１日            */ NEWYEAR_DAY             (NewYearDayBundle.class),
            /** 成人の日    ：１月の第２月曜日    */ COMING_OF_AGE_DAY       (ComingOfAgeDayBundle.class),
            /** 建国記念日  ：２月１１日          */ NATIONAL_FOUNDATION_DAY (NatinalFoundationBundle.class),
            /** 春分の日    ：３月 官報公示で決定 */ SPRING_EQUINOX_DAY      (SpringEquinoxBundle.class),
            /** 昭和の日    ：４月２９日          */ SHOUWA_DAY              (ShowaDayBundle.class),
            /** 憲法記念日  ：５月３日            */ KENPOUKINEN_DAY         (KenpoukikenDayBundle.class),
            /** みどりの日  ：５月４日            */ MIDORI_DAY              (MidoriDayBundle.class),
            /** こどもの日  ：５月５日            */ KODOMO_DAY              (KodomoDayBundle.class),
            /** 海の日      ：７月の第３月曜日    */ SEA_DAY                 (SeaDayBundle.class),
            /** 山の日      ：８月１１日.2016年以降 */ MOUNTAIN_DAY          (MountainDayBundle.class),
            /** 敬老の日    ：９月の第３月曜      */ RESPECT_FOR_AGE_DAY     (RespectForAgeDayBundle.class),
            /** 秋分の日    ：９月 官報公示で決定 */ AUTUMN_EQUINOX_DAY      (AutumnEquinoxBundle.class),
            /** 体育の日    ：１０月の第２月曜日  */ HEALTH_SPORTS_DAY       (HealthSportsDayBundle.class),
            /** 文化の日    ：１１月３日          */ CULTURE_DAY             (CultureDayBundle.class),
            /** 勤労感謝の日：１１月２３日        */ LABOR_THANKS_DAY        (LaborThanksDayBundle.class),
            /** 天皇誕生日  ：１２月２３日        */ TENNO_BIRTHDAY          (TennoBirthDayBundle.class)
            ;
            private Class<? extends HolidayBundle> cls;
            private HolidayType(Class<? extends HolidayBundle> cls){
                this.cls = cls;
            }
            public HolidayBundle getBundle(int year){
                try{
                    Constructor<?> ct = this.cls.getDeclaredConstructor(Holiday.class,int.class);
                    return (HolidayBundle)ct.newInstance(null,year);
                }catch(Exception e){
                    return null;
                }
            }
        }
        // 月→HolidayBundle class 参照 enum
        private enum MonthBundle{
            JANUARY        (NewYearDayBundle.class, ComingOfAgeDayBundle.class)
            , FEBRUARY     (NatinalFoundationBundle.class, TennoBirthDayBundle.class)
            , MARCH        (SpringEquinoxBundle.class)
            , APRIL        (ShowaDayBundle.class)
            , MAY          (KenpoukikenDayBundle.class, MidoriDayBundle.class, KodomoDayBundle.class)
            , JUNE         ()
            , JULY         (SeaDayBundle.class)
            , AUGUST       (MountainDayBundle.class)
            , SEPTEMBER    (RespectForAgeDayBundle.class, AutumnEquinoxBundle.class)
            , OCTOBER      (HealthSportsDayBundle.class)
            , NOVEMBER     (CultureDayBundle.class, LaborThanksDayBundle.class)
            , DECEMBER     ();
            //
            private Constructor<?>[] constructors;
            MonthBundle(Class<?>...clss){
                if (clss.length > 0){
                    this.constructors = new Constructor<?>[clss.length];
                    for(int i=0;i < clss.length;i++){
                        try{
                            this.constructors[i] = clss[i].getDeclaredConstructor(Holiday.class,int.class);
                        }catch(Exception e){}
                    }
                }
            }
            Constructor<?>[] getConstructors(){
                return this.constructors;
            }
        }
        //========================================================================
        /** 祝日Bundle抽象クラス */
        public abstract class HolidayBundle{
            int year;
            private Calendar mycal;
            public abstract int getDay();
            public abstract int getMonth();
            public abstract String getDescription();
            /** 対象年を指定するコンストラクタ
             * @param year 西暦４桁
             */
            public HolidayBundle(int year){
                this.year = year;
                this.mycal = Calendar.getInstance();
                this.mycal.set(this.year,this.getMonth()-1,this.getDay());
            }
            /** 振替休日の存在する場合、振替休日の日を返す。存在しない場合→ -1 を返す。*/
            public int getChangeDay(){
                if (this.getWeekDay()==Calendar.SUNDAY){
                    Calendar cal =  Calendar.getInstance();
                    cal.set(this.year,this.getMonth()-1,this.getDay());
                    cal.add(Calendar.DAY_OF_MONTH,1);
                    return cal.get(Calendar.DAY_OF_MONTH);
                }
                return -1;
            }
            /** 振替休日の存在する場合、振替休日のDateを返す。存在しない場合→ null を返す。*/
            public Date getChangeDate(){
                if (this.getWeekDay()==Calendar.SUNDAY){
                    Calendar cal =  Calendar.getInstance();
                    cal.set(this.year,this.getMonth()-1,this.getDay());
                    cal.add(Calendar.DAY_OF_MONTH,1);
                    return cal.getTime();
                }
                return null;
            }
            /** 祝日の曜日を Calendar.DAY_OF_WEEK に従って求める */
            public int getWeekDay(){
                return this.mycal.get(Calendar.DAY_OF_WEEK);
            }
            /** 祝日の Date を取得 */
            public Date getDate(){
                return this.mycal.getTime();
            }
        }

        /** 祝日、振替休日を含んで、Date配列で返す。*/
        public Date[] listHoliDays(){
            return this.holidayDates;
        }
        /**
         * 指定年、月の祝日、振替休日、国民の休日、日付(int)配列で返す
         * @param year 西暦４桁
         * @param calender_MONTH java.util.Calendar.MONTHによるフィールド値 0=１月、11=１２月
         * @return java.util.Calendar.DAY_OF_MONTH である配列
         */
        public static int[] listHoliDays(int year,int calender_MONTH){
            if (calender_MONTH < 0 || 11 < calender_MONTH){
                throw new IllegalArgumentException("calender_MONTH parameter Error");
            }
            if (year < 2016 && calender_MONTH == Calendar.AUGUST) return new int[]{};  // since ver 2.0
            MonthBundle mb = MonthBundle.valueOf(MONTH_NAMES[calender_MONTH]);
            if (year < 2019) {
                if (calender_MONTH == Calendar.FEBRUARY) {
                    Set<Integer> set = new TreeSet<Integer>();
                    try{
                        Constructor<?>[] constructors = new Constructor<?>[]{ NatinalFoundationBundle.class.getDeclaredConstructor(Holiday.class, int.class) };
                        HolidayBundle h = (HolidayBundle)constructors[0].newInstance(null, year);
                        set.add(h.getDay());
                        int chgday = h.getChangeDay();
                        if (chgday > 0) set.add(chgday);
                        if (set.size()==0) return new int[]{};
                        int[] rtns = new int[set.size()];
                        int i=0;
                        for(Iterator<Integer> it = set.iterator(); it.hasNext(); i++){
                            rtns[i] = it.next();
                        }
                        return rtns;
                    } catch(Exception e){
                    }
                } else if (calender_MONTH == Calendar.DECEMBER) {
                    Set<Integer> set = new TreeSet<Integer>();
                    try{
                        Constructor<?>[] constructors = new Constructor<?>[]{ TennoBirthDayBundle.class.getDeclaredConstructor(Holiday.class, int.class) };
                        HolidayBundle h = (HolidayBundle)constructors[0].newInstance(null, year);
                        set.add(h.getDay());
                        int chgday = h.getChangeDay();
                        if (chgday > 0) set.add(chgday);
                        if (set.size()==0) return new int[]{};
                        int[] rtns = new int[set.size()];
                        int i=0;
                        for(Iterator<Integer> it = set.iterator(); it.hasNext(); i++){
                            rtns[i] = it.next();
                        }
                        return rtns;
                    } catch(Exception e){
                    }
                }
            } else if (year == 2019) {
                // 2019年は天皇誕生日無し ver 3.1
                if (calender_MONTH == Calendar.FEBRUARY) {
                    Set<Integer> set = new TreeSet<Integer>();
                    try{
                        Constructor<?>[] constructors = new Constructor<?>[]{ NatinalFoundationBundle.class.getDeclaredConstructor(Holiday.class, int.class) };
                        HolidayBundle h = (HolidayBundle)constructors[0].newInstance(null, year);
                        set.add(h.getDay());
                        int chgday = h.getChangeDay();
                        if (chgday > 0) set.add(chgday);
                        if (set.size()==0) return new int[]{};
                        int[] rtns = new int[set.size()];
                        int i=0;
                        for(Iterator<Integer> it = set.iterator(); it.hasNext(); i++){
                            rtns[i] = it.next();
                        }
                        return rtns;
                    } catch(Exception e){
                    }
                } else if (calender_MONTH == Calendar.MAY) {
                    return new int[]{ 1, 2, 3, 4, 5, 6 };
                } else if (calender_MONTH == Calendar.SEPTEMBER) {
                    return new int[]{ 16, 23 };
                } else if (calender_MONTH == Calendar.OCTOBER) {
                    return new int[]{ 14, 22 };
                }
            } else if (year == 2020) {
                if (calender_MONTH == Calendar.JULY) {
                    return new int[]{ 23, 24 };
                } else if (calender_MONTH == Calendar.OCTOBER) {
                    return new int[]{};
                }
            }
            Constructor<?>[] constructors = mb.getConstructors();
            if (constructors==null) return new int[]{};
            Set<Integer> set = new TreeSet<Integer>();
            for(int i=0;i < constructors.length;i++){
                try{
                    HolidayBundle b = (HolidayBundle)constructors[i].newInstance(null,year);
                    set.add(b.getDay());
                    int chgday = b.getChangeDay();
                    if (chgday > 0) set.add(chgday);
                }catch(Exception e){
                }
            }
            // 現在、国民の休日の発生は９月しかない
            if (calender_MONTH == Calendar.SEPTEMBER){
                Date[] ds = getNatinalHoliday(year);
                if (ds.length > 0){
                    Calendar cal = Calendar.getInstance();
                    for(Date d:ds){
                        cal.setTime(d);
                        set.add(cal.get(Calendar.DAY_OF_MONTH));
                    }
                }
            }
            // ver3.3 平成の次の即位日
            if (year==2019){
                if (calender_MONTH == Calendar.APRIL){
                    set.add(30);
                }else if(calender_MONTH == Calendar.MAY){
                    set.add(2);
                }
            }
            if (set.size()==0) return new int[]{};
            int[] rtns = new int[set.size()];
            int i=0;
            for(Iterator<Integer> it = set.iterator(); it.hasNext(); i++){
                rtns[i] = it.next();
            }
            return rtns;
        }

        /**
         * 指定年、月の祝日、振替休日、国民の休日、日付(Date)配列で返す
         * @param year 西暦４桁
         * @param calender_MONTH java.util.Calendar.MONTHによるフィールド値 0=１月、11=１２月
         * @return Date配列
         * @since 3.1 2019年は天皇誕生日無し
         * @since 3.8 2019年対応
         */
        public static Date[] listHoliDayDates(int year,int calender_MONTH){
            if (calender_MONTH < 0 || 11 < calender_MONTH){
                throw new IllegalArgumentException("calender_MONTH parameter Error");
            }
            if (year < 2016 && calender_MONTH == Calendar.AUGUST) return new Date[]{}; // since ver 2.0
            MonthBundle mb = MonthBundle.valueOf(MONTH_NAMES[calender_MONTH]);
            if (year < 2019) {
                if (calender_MONTH == Calendar.FEBRUARY) {
                    Set<Date> set = new TreeSet<Date>();
                    try{
                        Constructor<?>[] constructors = new Constructor<?>[]{ NatinalFoundationBundle.class.getDeclaredConstructor(Holiday.class, int.class) };
                        HolidayBundle h = (HolidayBundle)constructors[0].newInstance(null, year);
                        set.add(h.getDate());
                        Date chgdt = h.getChangeDate();
                        if (chgdt != null) set.add(chgdt);
                        Date[] rtns = new Date[set.size()];
                        int i=0;
                        for(Iterator<Date> it=set.iterator();it.hasNext();i++){
                            rtns[i] = it.next();
                        }
                        return rtns;
                    } catch(Exception e){
                    }
                } else if (calender_MONTH == Calendar.DECEMBER) {
                    Set<Date> set = new TreeSet<Date>();
                    try{
                        Constructor<?>[] constructors = new Constructor<?>[]{ TennoBirthDayBundle.class.getDeclaredConstructor(Holiday.class, int.class) };
                        HolidayBundle h = (HolidayBundle)constructors[0].newInstance(null, year);
                        set.add(h.getDate());
                        Date chgdt = h.getChangeDate();
                        if (chgdt != null) set.add(chgdt);
                        Date[] rtns = new Date[set.size()];
                        int i=0;
                        for(Iterator<Date> it=set.iterator();it.hasNext();i++){
                            rtns[i] = it.next();
                        }
                        return rtns;
                    } catch(Exception e){
                    }
                }
            } else if (year == 2019) {
                // 2019年は天皇誕生日無し ver 3.1
                if (calender_MONTH == Calendar.FEBRUARY) {
                    Set<Date> set = new TreeSet<Date>();
                    try{
                        Constructor<?>[] constructors = new Constructor<?>[]{ NatinalFoundationBundle.class.getDeclaredConstructor(Holiday.class, int.class) };
                        HolidayBundle h = (HolidayBundle)constructors[0].newInstance(null, year);
                        set.add(h.getDate());
                        Date chgdt = h.getChangeDate();
                        if (chgdt != null) set.add(chgdt);
                        Date[] rtns = new Date[set.size()];
                        int i=0;
                        for(Iterator<Date> it=set.iterator();it.hasNext();i++){
                            rtns[i] = it.next();
                        }
                        return rtns;
                    } catch(Exception e){
                    }
                } else if (calender_MONTH == Calendar.MAY) {
                    return new Date[]{ toDate(2019, 5, 1), toDate(2019, 5, 2), toDate(2019, 5, 3), toDate(2019, 5, 4), toDate(2019, 5, 5), toDate(2019, 5, 6) };
                } else if(calender_MONTH == Calendar.SEPTEMBER) {
                    return new Date[]{ toDate(2019, 9, 16), toDate(2019, 9, 23) };
                } else if(calender_MONTH == Calendar.OCTOBER) {
                    return new Date[]{ toDate(2019, 10, 14), toDate(2019, 10, 22) };
                }
            } else if (year == 2020) {
                if (calender_MONTH == Calendar.JULY) {
                    return new Date[]{ toDate(2020, 7, 23), toDate(2020, 7, 24) };
                } else if (calender_MONTH == Calendar.OCTOBER) {
                    return new Date[]{};
                }
            }
            Constructor<?>[] constructors = mb.getConstructors();
            if (constructors==null) return new Date[]{}; // for ver 2.0
            Set<Date> set = new TreeSet<Date>();
            for(int i=0;i < constructors.length;i++){
                try{
                    HolidayBundle b = (HolidayBundle)constructors[i].newInstance(null,year);
                    set.add(b.getDate());
                    Date chgdt = b.getChangeDate();
                    if (chgdt != null) set.add(chgdt);
                }catch(Exception e){
                }
            }
            // 現在、国民の休日の発生は９月しかない
            if (calender_MONTH==Calendar.SEPTEMBER){
                Date[] ds = getNatinalHoliday(year);
                if (ds.length > 0){
                    for(Date d:ds) set.add(d);
                }
            }
            // ver3.3 平成の次の即位日
            if (year == 2019){
                if (calender_MONTH == Calendar.APRIL){
                    set.add(toDate(2019, 4, 30));
                }else if(calender_MONTH == Calendar.MAY){
                    set.add(toDate(2019, 5, 2));
                }
            }
            if (set.size()==0) return new Date[]{};
            Date[] rtns = new Date[set.size()];
            int i=0;
            for(Iterator<Date> it=set.iterator();it.hasNext();i++){
                rtns[i] = it.next();
            }
            return rtns;
        }

        /** 日付フォーマット yyyy/MM/dd */
        public static SimpleDateFormat YMD_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
        /** Calendar.MONTH に沿った月名の配列 */
        public static String[] MONTH_NAMES = {"JANUARY","FEBRUARY","MARCH","APRIL","MAY","JUNE","JULY","AUGUST","SEPTEMBER","OCTOBER","NOVEMBER","DECEMBER"};

        /** 指定日が祝日なら、祝日名を返す。（指定日による祝日、振替休日チェックの為）
         * @parama 指定日
         * @return 祝日名を返す。祝日、振替休日に該当しなければ、null を返す。
         */
        public static String queryHoliday(Date dt){
            Calendar cal = Calendar.getInstance();
            cal.setTime(dt);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            if (year < 2016 && month == Calendar.AUGUST) return null;   // since ver 2.0
            // ver 3.3
            if (year == 2019){
                int y = cal.get(Calendar.YEAR);
                int m = cal.get(Calendar.MONTH);
                int d = cal.get(Calendar.DAY_OF_MONTH);
                dt = toDate(y, m, d);

                if (dt.compareTo(toDate(2019, 4, 30)) == 0) return "国民の休日";
                if (dt.compareTo(toDate(2019, 5, 1)) == 0) return "平成の次の即位日"; // ver 3.6
                if (dt.compareTo(toDate(2019, 5, 2)) == 0) return "国民の休日"; // ver 3.6
                if (dt.compareTo(toDate(2019, 10, 22)) == 0) return "即位礼正殿の儀"; // ver 3.6
            }
            MonthBundle mb = MonthBundle.valueOf(MONTH_NAMES[month]);
            if (year < 2019) {
                if (month == Calendar.FEBRUARY) {
                    try{
                        Constructor<?>[] constructors = new Constructor<?>[]{ NatinalFoundationBundle.class.getDeclaredConstructor(Holiday.class, int.class) };
                        HolidayBundle h = (HolidayBundle)constructors[0].newInstance(null, year);
                        if (cal.get(Calendar.DAY_OF_MONTH)==h.getDay()){ return h.getDescription(); }
                        if (cal.get(Calendar.DAY_OF_MONTH)==h.getChangeDay()){ return "振替休日"+"（"+h.getDescription()+"）"; }
                    }catch(Exception e){
                    }
                    return null;
                } else if (month == Calendar.DECEMBER) {
                    try{
                        Constructor<?>[] constructors = new Constructor<?>[]{ TennoBirthDayBundle.class.getDeclaredConstructor(Holiday.class, int.class) };
                        HolidayBundle h = (HolidayBundle)constructors[0].newInstance(null, year);
                        if (cal.get(Calendar.DAY_OF_MONTH)==h.getDay()){ return h.getDescription(); }
                        if (cal.get(Calendar.DAY_OF_MONTH)==h.getChangeDay()){ return "振替休日"+"（"+h.getDescription()+"）"; }
                    }catch(Exception e){
                    }
                }
            } else if (year == 2019) {
                // 2019年は天皇誕生日無し
                if (month == Calendar.FEBRUARY) {
                    try{
                        Constructor<?>[] constructors = new Constructor<?>[]{ NatinalFoundationBundle.class.getDeclaredConstructor(Holiday.class, int.class) };
                        HolidayBundle h = (HolidayBundle)constructors[0].newInstance(null, year);
                        if (cal.get(Calendar.DAY_OF_MONTH)==h.getDay()){ return h.getDescription(); }
                        if (cal.get(Calendar.DAY_OF_MONTH)==h.getChangeDay()){ return "振替休日"+"（"+h.getDescription()+"）"; }
                    }catch(Exception e){
                    }
                    return null;
                }
            } else if (year == 2020) {
                // 2020年に限り、体育の日（スポーツの日）は7月
                if (month == Calendar.JULY) {
                    try{
                        Constructor<?>[] constructors = new Constructor<?>[]{ HealthSportsDayBundle.class.getDeclaredConstructor(Holiday.class, int.class) };
                        HolidayBundle h = (HolidayBundle)constructors[0].newInstance(null, year);
                        if (cal.get(Calendar.DAY_OF_MONTH)==h.getDay()){ return h.getDescription(); }
                        //if (cal.get(Calendar.DAY_OF_MONTH)==h.getChangeDay()){ return "振替休日"+"（"+h.getDescription()+"）"; }
                    }catch(Exception e){
                    }
                } else if (month == Calendar.OCTOBER) {
                    return null;
                }
            }
            Constructor<?>[] constructors = mb.getConstructors();
            if (constructors==null){
                return null; // 祝日でない！
            }
            int targetDay = cal.get(Calendar.DAY_OF_MONTH);
            int targetYear = cal.get(Calendar.YEAR);
            for(int i=0;i < constructors.length;i++){
                try{
                    HolidayBundle h = (HolidayBundle)constructors[i].newInstance(null,targetYear);
                    if (targetDay==h.getDay()){ return h.getDescription(); }
                    if (targetDay==h.getChangeDay()){ return "振替休日"+"（"+h.getDescription()+"）"; }
                }catch(Exception e){
                }
            }
            Date[] natinalHolidayDates = getNatinalHoliday(targetYear);
            if (natinalHolidayDates != null){
                String targetDateStr = YMD_FORMAT.format(dt);
                for(int i=0;i < natinalHolidayDates.length;i++){
                    if (targetDateStr.equals(YMD_FORMAT.format(natinalHolidayDates[i]))){
                        return "国民の休日";
                    }
                }
            }
            return null;
        }
        /**
         * 指定日が祝日法による祝日かどうか.
         * @param dt 指定日
         * @return true=祝日である。
         */
        public static boolean isHoliday(Date dt){
            Calendar cal = Calendar.getInstance();
            cal.setTime(dt);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            if (year < 2016 && month == Calendar.AUGUST) return false;
            MonthBundle mb = MonthBundle.valueOf(MONTH_NAMES[month]);
            if (year < 2019) {
                if (month == Calendar.FEBRUARY) {
                    try{
                        Constructor<?>[] constructors = new Constructor<?>[]{ NatinalFoundationBundle.class.getDeclaredConstructor(Holiday.class, int.class) };
                        HolidayBundle h = (HolidayBundle)constructors[0].newInstance(null, year);
                        if (cal.get(Calendar.DAY_OF_MONTH)==h.getDay()){ return true;}
                        return cal.get(Calendar.DAY_OF_MONTH)==h.getChangeDay();
                    }catch(Exception e){
                    }
                } if (month == Calendar.DECEMBER) {
                    try{
                        Constructor<?>[] constructors = new Constructor<?>[]{ TennoBirthDayBundle.class.getDeclaredConstructor(Holiday.class, int.class) };
                        HolidayBundle h = (HolidayBundle)constructors[0].newInstance(null, year);
                        if (cal.get(Calendar.DAY_OF_MONTH)==h.getDay()){ return true;}
                        return cal.get(Calendar.DAY_OF_MONTH)==h.getChangeDay();
                    }catch(Exception e){
                    }
                }
            } else if (year == 2019) {
                // 2019年は天皇誕生日無し
                if (month == Calendar.FEBRUARY) {
                    try{
                        Constructor<?>[] constructors = new Constructor<?>[]{ NatinalFoundationBundle.class.getDeclaredConstructor(Holiday.class, int.class) };
                        HolidayBundle h = (HolidayBundle)constructors[0].newInstance(null, year);
                        if (cal.get(Calendar.DAY_OF_MONTH)==h.getDay()){ return true;}
                        return cal.get(Calendar.DAY_OF_MONTH)==h.getChangeDay();
                    }catch(Exception e){
                    }
                }
            } else if (year == 2020) {
                // 2020年に限り、体育の日（スポーツの日）は7月
                if (month == Calendar.JULY) {
                    try{
                        Constructor<?>[] constructors = new Constructor<?>[]{ HealthSportsDayBundle.class.getDeclaredConstructor(Holiday.class, int.class) };
                        HolidayBundle h = (HolidayBundle)constructors[0].newInstance(null, year);
                        if (cal.get(Calendar.DAY_OF_MONTH)==h.getDay()){ return true;}
                    }catch(Exception e){
                    }
                } else if (month == Calendar.OCTOBER) {
                    return false;
                }
            }
            Constructor<?>[] constructors = mb.getConstructors();
            if (constructors==null){
                return false; // 祝日でない！
            }
            int targetDay = cal.get(Calendar.DAY_OF_MONTH);
            int targetYear = cal.get(Calendar.YEAR);
            for(int i=0;i < constructors.length;i++){
                try{
                    HolidayBundle h = (HolidayBundle)constructors[i].newInstance(null,targetYear);
                    if (targetDay==h.getDay()){ return true; }
                }catch(Exception e){
                }
            }
            Date[] natinalHolidayDates = getNatinalHoliday(targetYear);
            if (natinalHolidayDates != null){
                String targetDateStr = YMD_FORMAT.format(dt);
                for(int i=0;i < natinalHolidayDates.length;i++){
                    if (targetDateStr.equals(YMD_FORMAT.format(natinalHolidayDates[i]))){
                        return true;
                    }
                }
            }
            // ver 3.3
            if (year == 2019){
                int y = cal.get(Calendar.YEAR);
                int m = cal.get(Calendar.MONTH);
                int d = cal.get(Calendar.DAY_OF_MONTH);
                dt = toDate(y, m, d);

                if (dt.compareTo(toDate(2019, 4, 30)) == 0) return true;
                if (dt.compareTo(toDate(2019, 5, 2)) == 0) return true;
                if (dt.compareTo(toDate(2019, 10, 22)) == 0) return true;
            }
            return false;
        }

        public static String[] WEEKDAYS_JA = {"日","月","火","水","木","金","土" };
        /** 曜日String算出 Japanese */
        public static String dateOfWeekJA(Date dt){
            Calendar cal = Calendar.getInstance();
            cal.setTime(dt);
            return WEEKDAYS_JA[cal.get(Calendar.DAY_OF_WEEK)-1];
        }
        public static String[] WEEKDAYS_SIMPLE = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat" };
        /** 曜日String算出 */
        public static String dateOfWeekSimple(Date dt){
            Calendar cal = Calendar.getInstance();
            cal.setTime(dt);
            return WEEKDAYS_SIMPLE[cal.get(Calendar.DAY_OF_WEEK)-1];
        }

        /** 指定年→国民の休日のみのDate[]の取得 */
        public static Date[] getNatinalHoliday(int year){
            if (year==2019){
                // ver 3.5
                return new Date[]{ toDate(2019, 4, 30), toDate(2019, 5, 2) };
            }
            // 2019年以外は、敬老の日と秋分の日が１日で挟まれた場合のみ。
            HolidayBundle k = HolidayType.RESPECT_FOR_AGE_DAY.getBundle(year);
            HolidayBundle a = HolidayType.AUTUMN_EQUINOX_DAY.getBundle(year);
            int aday = a.getDay();
            int kday = k.getDay();
            int chgday = k.getChangeDay();
            if ((aday - kday)==2){
                Calendar c = Calendar.getInstance();
                c.set(year,Calendar.SEPTEMBER,kday+1);
                return new Date[]{c.getTime()};
            }else if (chgday > 0 && ((aday - chgday)==2)){
                Calendar c = Calendar.getInstance();
                c.set(year,Calendar.SEPTEMBER,chgday+1);
                return new Date[]{c.getTime()};
            }
            return new Date[]{};
        }
        //========================================================================
        // 元旦
        class NewYearDayBundle extends HolidayBundle{
            public NewYearDayBundle(int year){
                super(year);
            }
            @Override
            public int getDay(){
                return 1;
            }
            @Override
            public int getMonth(){
                return 1;
            }
            @Override
            public String getDescription(){
                return "元旦";
            }
        }
        // 成人の日
        class ComingOfAgeDayBundle extends HolidayBundle{
            public ComingOfAgeDayBundle(int year){
                super(year);
            }
            /* １月第２月曜日の日付を求める */
            @Override
            public int getDay(){
                Calendar cal = Calendar.getInstance();
                cal.set(super.year,Calendar.JANUARY,1);
                int wday = cal.get(Calendar.DAY_OF_WEEK);
                return wday > Calendar.MONDAY ? (7*2+1)-(wday - Calendar.MONDAY) : 7+1+(Calendar.MONDAY - wday);
            }
            @Override
            public int getMonth(){
                return 1;
            }
            @Override
            public String getDescription(){
                return "成人の日";
            }
        }
        // 建国記念日
        class NatinalFoundationBundle extends HolidayBundle{
            public NatinalFoundationBundle(int year){
                super(year);
            }
            @Override
            public int getDay(){
                return 11;
            }
            @Override
            public int getMonth(){
                return 2;
            }
            @Override
            public String getDescription(){
                return "建国記念日";
            }
        }
        // 春分の日
        // 『海上保安庁水路部 暦計算研究会編 新こよみ便利帳』による計算式
        // さらに、1979年以前を無視！～2150年まで有効
        class SpringEquinoxBundle extends HolidayBundle{
            public SpringEquinoxBundle(int year){
                super(year);
            }
            @Override
            public int getDay(){
                if (super.year <= 2099){
                    return (int)(20.8431 + (0.242194 * (super.year - 1980)) - ((super.year - 1980 )/4));
                }
                return (int)(21.851 + (0.242194 * (super.year - 1980)) - ((super.year - 1980)/4));
            }
            @Override
            public int getMonth(){
                return 3;
            }
            @Override
            public String getDescription(){
                return "春分の日";
            }
        }
        // 昭和の日
        class ShowaDayBundle extends HolidayBundle{
            public ShowaDayBundle(int year){
                super(year);
            }
            @Override
            public int getDay(){
                return 29;
            }
            @Override
            public int getMonth(){
                return 4;
            }
            @Override
            public String getDescription(){
                return "昭和の日";
            }
        }
        // 憲法記念日
        class KenpoukikenDayBundle extends HolidayBundle{
            public KenpoukikenDayBundle(int year){
                super(year);
            }
            @Override
            public int getDay(){
                return 3;
            }
            @Override
            public int getMonth(){
                return 5;
            }
            // ５月３日＝Sunday の振替は、６日
            @Override
            public int getChangeDay(){
                if (this.getWeekDay()==Calendar.SUNDAY){
                    return 6;
                }
                return -1;
            }
            @Override
            public Date getChangeDate(){
                if (this.getWeekDay()==Calendar.SUNDAY){
                    Calendar cal =  Calendar.getInstance();
                    cal.set(this.year,this.getMonth()-1,6);
                    return cal.getTime();
                }
                return null;
            }
            @Override
            public String getDescription(){
                return "憲法記念日";
            }
        }
        // みどりの日
        class MidoriDayBundle extends HolidayBundle{
            public MidoriDayBundle(int year){
                super(year);
            }
            @Override
            public int getDay(){
                return 4;
            }
            @Override
            public int getMonth(){
                return 5;
            }
            // ５月４日＝Sunday の振替は、６日
            @Override
            public int getChangeDay(){
                if (this.getWeekDay()==Calendar.SUNDAY){
                    return 6;
                }
                return -1;
            }
            @Override
            public Date getChangeDate(){
                if (this.getWeekDay()==Calendar.SUNDAY){
                    Calendar cal =  Calendar.getInstance();
                    cal.set(this.year,this.getMonth()-1,6);
                    return cal.getTime();
                }
                return null;
            }
            @Override
            public String getDescription(){
                return "みどりの日";
            }
        }
        // こどもの日
        class KodomoDayBundle extends HolidayBundle{
            public KodomoDayBundle(int year){
                super(year);
            }
            @Override
            public int getDay(){
                return 5;
            }
            @Override
            public int getMonth(){
                return 5;
            }
            @Override
            public String getDescription(){
                return "こどもの日";
            }
        }
        // 海の日
        // 2020年に限り、「海の日」は7月23日
        class SeaDayBundle extends HolidayBundle{
            public SeaDayBundle(int year){
                super(year);
            }
            /* ７月第３月曜日の日付を求める */
            @Override
            public int getDay(){
                if (super.year == 2020)
                {
                    return 23;
                }

                Calendar cal = Calendar.getInstance();
                cal.set(super.year,Calendar.JULY,1);
                int wday = cal.get(Calendar.DAY_OF_WEEK);
                return wday > Calendar.MONDAY ? (7*3+1)-(wday - Calendar.MONDAY) : 14+1+(Calendar.MONDAY - wday);
            }
            @Override
            public int getMonth(){
                return 7;
            }
            @Override
            public String getDescription(){
                return "海の日";
            }
        }
        // 山の日
        class MountainDayBundle extends HolidayBundle{
            public MountainDayBundle(int year){
                super(year);
            }
            @Override
            public int getDay(){
                return super.year==2020 ? 10 : 11;
            }
            @Override
            public int getMonth(){
                return 8;
            }
            @Override
            public String getDescription(){
                return "山の日";
            }
        }
        // 敬老の日
        class RespectForAgeDayBundle extends HolidayBundle{
            public RespectForAgeDayBundle(int year){
                super(year);
            }
            /* ９月第３月曜日の日付を求める */
            @Override
            public int getDay(){
                Calendar cal = Calendar.getInstance();
                cal.set(super.year,Calendar.SEPTEMBER,1);
                int wday = cal.get(Calendar.DAY_OF_WEEK);
                return wday > Calendar.MONDAY ? (7*3+1)-(wday - Calendar.MONDAY) : 14+1+(Calendar.MONDAY - wday);
            }
            @Override
            public int getMonth(){
                return 9;
            }
            @Override
            public String getDescription(){
                return "敬老の日";
            }
        }
        // 秋分の日
        // 『海上保安庁水路部 暦計算研究会編 新こよみ便利帳』による計算式
        // さらに、1979年以前を無視！～2150年まで有効
        class AutumnEquinoxBundle extends HolidayBundle{
            public AutumnEquinoxBundle(int year){
                super(year);
            }
            @Override
            public int getDay(){
                if (super.year <= 2099){
                    return (int)(23.2488 + (0.242194 * (super.year - 1980)) - ((super.year - 1980)/4));
                }
                return (int)(24.2488 + (0.242194 * (super.year - 1980)) - ((super.year - 1980)/4));
            }
            @Override
            public int getMonth(){
                return 9;
            }
            @Override
            public String getDescription(){
                return "秋分の日";
            }
        }
        // 体育の日
        // 2020年に限り、「体育の日（スポーツの日）」は7月24日
        class HealthSportsDayBundle extends HolidayBundle{
            public HealthSportsDayBundle(int year){
                super(year);
            }
            /* １０月第２月曜日の日付を求める */
            @Override
            public int getDay(){
                if (super.year == 2020)
                {
                    return 24;
                }

                Calendar cal = Calendar.getInstance();
                cal.set(super.year,Calendar.OCTOBER,1);
                int wday = cal.get(Calendar.DAY_OF_WEEK);
                return wday > Calendar.MONDAY ? (7*2+1)-(wday - Calendar.MONDAY) : 7+1+(Calendar.MONDAY - wday);
            }
            @Override
            public int getMonth(){
                return super.year==2020 ? 7 : 10;
            }
            @Override
            public String getDescription(){
                return "体育の日";
            }
        }
        // 文化の日
        class CultureDayBundle extends HolidayBundle{
            public CultureDayBundle(int year){
                super(year);
            }
            @Override
            public int getDay(){
                return 3;
            }
            @Override
            public int getMonth(){
                return 11;
            }
            @Override
            public String getDescription(){
                return "文化の日";
            }
        }
        // 勤労感謝の日
        class LaborThanksDayBundle extends HolidayBundle{
            public LaborThanksDayBundle(int year){
                super(year);
            }
            @Override
            public int getDay(){
                return 23;
            }
            @Override
            public int getMonth(){
                return 11;
            }
            @Override
            public String getDescription(){
                return "勤労感謝の日";
            }
        }
        // 天皇誕生日
        class TennoBirthDayBundle extends HolidayBundle{
            public TennoBirthDayBundle(int year){
                super(year);
            }
            @Override
            public int getDay(){
                return 23;
            }
            @Override
            public int getMonth(){
                //return 12;
                // ver 3.1
                return super.year < 2019 ? 12 : 2;
            }
            @Override
            public String getDescription(){
                return "天皇誕生日";
            }
        }
        /**
         * 平成の次の即位日
         * 注意： 2019年のみ
         * ver 3.3
         */
        class HeiseiNextDayBundle extends HolidayBundle{
            public HeiseiNextDayBundle(int year){
                super(2019);
            }
            @Override
            public int getDay(){
                return 1;
            }
            @Override
            public int getMonth(){
                return 5;
            }
            @Override
            public String getDescription(){
                return "平成の次の即位日";
            }
        }
        /**
         * 即位礼正殿の儀
         * 注意： 2019年のみ
         * ver 3.3
         */
        class SokuiReiSeidenDayBundle extends HolidayBundle{
            public SokuiReiSeidenDayBundle(int year){
                super(2019);
            }
            @Override
            public int getDay(){
                return 22;
            }
            @Override
            public int getMonth(){
                return 10;
            }
            @Override
            public String getDescription(){
                return "即位礼正殿の儀";
            }
        }
        /**
         * 平成の次の即位日の前日の休日
         * 注意： 2019年のみ
         * ver 3.5
         */
        class HeisetNextPrevBundle extends HolidayBundle{
            public HeisetNextPrevBundle(int year){
                super(2019);
            }
            @Override
            public int getDay(){
                return 30;
            }
            @Override
            public int getMonth(){
                return 4;
            }
            @Override
            public String getDescription(){
                return "国民の休日";
            }
        }
        /**
         * 平成の次の即位日の翌日の休日
         * 注意： 2019年のみ
         * ver 3.5
         */
        class HeisetNextNextBundle extends HolidayBundle{
            public HeisetNextNextBundle(int year){
                super(2019);
            }
            @Override
            public int getDay(){
                return 2;
            }
            @Override
            public int getMonth(){
                return 5;
            }
            @Override
            public String getDescription(){
                return "国民の休日";
            }
        }
    }
}
