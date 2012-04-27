/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2009, OpenRemote Inc.
*
* See the contributors.txt file in the distribution for a
* full listing of individual contributors.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.openremote.modeler.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * The Class DateUtil.
 */
public final class DateUtil {

   /** default date format pattern. */
   public static final String DATE_FORMAT = "yyyy-MM-dd";
   
   /** The Constant DATE_TIME_FORMAT. */
   public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
   
   /** The Constant DATE_TIME_FORMAT2. */
   public static final String DATE_TIME_FORMAT2 = "MM-dd HH:mm";
   
   /** The Constant TIME_FORMAT. */
   public static final String TIME_FORMAT = "HH:mm";
   
   /** The Constant DATE_FORMAT2. */
   public static final String DATE_FORMAT2 = "MM/dd/yyyy";
   
   /** The Constant DATE_FORMAT_SLASH. */
   public static final String DATE_FORMAT_SLASH = "yyyy/MM/dd";
   
   /** The Constant MONTH_DAY_SLASH. */
   public static final String MONTH_DAY_SLASH = "MM/dd";
   
   /**
    * Instantiates a new date util.
    */
   private DateUtil() {
   }

   /**
    * parse date with the default pattern.
    * 
    * @param date string date
    * 
    * @return the parsed date
    */
   public static Date parseDate(String date) {
      SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
      try {
         return format.parse(date);
      } catch (ParseException e) {
         return new Date();
      }
   }

   /**
    * Parses the slash date.
    * 
    * @param date the date
    * 
    * @return the date
    */
   public static Date parseSlashDate(String date) {
      SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_SLASH);
      try {
         return format.parse(date);
      } catch (ParseException e) {
         return new Date();
      }
   }

   /**
    * Parses the due date.
    * 
    * @param date the date
    * 
    * @return the date
    */
   public static Date parseDueDate(String date) {
      SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT2);
      try {
         return format.parse(date);
      } catch (ParseException e) {
         return new Date();
      }
   }

   /**
    * format date with the default pattern.
    * 
    * @param date the date that want to format to string
    * 
    * @return the formated date
    */
   public static String formatDate(Date date) {
      SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
      return format.format(date);
   }

   /**
    * Format slash date.
    * 
    * @param date the date
    * 
    * @return the string
    */
   public static String formatSlashDate(Date date) {
      SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_SLASH);
      return format.format(date);
   }

   /**
    * Format time.
    * 
    * @param date the date
    * 
    * @return the string
    */
   public static String formatTime(Date date) {
      SimpleDateFormat format = new SimpleDateFormat(TIME_FORMAT);
      return format.format(date);
   }

   /**
    * format date with the given pattern.
    * 
    * @param date the date that want to format to string
    * @param pattern the formated pattern
    * 
    * @return the formated date
    */
   public static String formatDate(Date date, String pattern) {
      SimpleDateFormat format = new SimpleDateFormat(pattern);
      return format.format(date);
   }

   /**
    * get current date.
    * 
    * @return the string of current date
    */
   public static String getCurrentDateStr() {
      return formatDate(new Date());
   }

   /**
    * Gets the current date slash str.
    * 
    * @return the current date slash str
    */
   public static String getCurrentDateSlashStr() {
      return formatSlashDate(new Date());
   }

   /**
    * add day to the date.
    * 
    * @param date the added date
    * @param number the number to add to the date
    * 
    * @return the added date
    */
   public static Date addDate(Date date, int number) {
      Calendar calendar = getDefaultCalendar();
      calendar.setTime(date);
      calendar.add(Calendar.DATE, number);
      return calendar.getTime();
   }

   /**
    * get the default calendar.
    * 
    * @return the calendar instance
    */
   public static Calendar getDefaultCalendar() {
      Calendar calendar = Calendar.getInstance();
      calendar.setFirstDayOfWeek(Calendar.MONDAY);
      return calendar;
   }

   /**
    * format the date into string value.
    * 
    * @param calendar the formated calendar
    * 
    * @return the string date
    */
   public static String getStringDate(Calendar calendar) {
      int year = calendar.get(Calendar.YEAR);
      int month = calendar.get(Calendar.MONTH) + 1;
      int day = calendar.get(Calendar.DAY_OF_MONTH);
      return year + "-" + getNiceString(month) + "-" + getNiceString(day);
   }

   /**
    * according to the pattern yyyy-MM-dd.
    * 
    * @param value the value
    * 
    * @return the formated value
    */
   public static String getNiceString(int value) {
      String str = "00" + value;
      return str.substring(str.length() - 2, str.length());
   }

   /**
    * get calendar from date.
    * 
    * @param date the passing date
    * 
    * @return the calendar instance
    */
   public static Calendar getCalendarFromDate(Date date) {
      Calendar calendar = getDefaultCalendar();
      calendar.setTime(date);
      return calendar;
   }
   
   /**
    * Gets the year.
    * 
    * @param date the date
    * 
    * @return the year
    */
   public static int getYear(Date date) {
      Calendar calendar = getCalendarFromDate(date);
      return calendar.get(Calendar.YEAR);
   }

   /**
    * Gets the month.
    * 
    * @param date the date
    * 
    * @return the month
    */
   public static int getMonth(Date date) {
      Calendar calendar = getCalendarFromDate(date);
      return calendar.get(Calendar.MONTH) + 1;
   }

   /**
    * Gets the day of month.
    * 
    * @param date the date
    * 
    * @return the day of month
    */
   public static int getDayOfMonth(Date date) {
      Calendar calendar = getCalendarFromDate(date);
      return calendar.get(Calendar.DAY_OF_MONTH);
   }

   /**
    * Gets the hour.
    * 
    * @param now the now
    * 
    * @return the hour
    */
   public static int getHour(Date now) {
      Calendar calendar = getCalendarFromDate(now);
      return calendar.get(Calendar.HOUR_OF_DAY);
   }

   /**
    * dateSrc is less or equal (le:lessequal)dateDes.
    * 
    * @param dateDes the date des
    * @param dateSrc the date src
    * 
    * @return true, if le dates
    */
   public static boolean leDates(Date dateSrc, Date dateDes) {
      return dateSrc.compareTo(dateDes) <= 0 ? true : false;
   }

   /**
    * dateSrc is great or equal (gt:great)dateDes.
    * 
    * @param dateSrc the date src
    * @param dateDes the date des
    * 
    * @return true, if gt
    */
   public static boolean gt(Date dateSrc, Date dateDes) {
      return dateSrc.compareTo(dateDes) > 0 ? true : false;
   }

   /**
    * Gets the current date.
    * 
    * @return the current date
    */
   public static Date getCurrentDate() {
      Calendar calendar = getCalendarFromDate(new Date());
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      return calendar.getTime();
   }

}
