package com.zimbra.cs.account.accesscontrol;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.DateUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.AttributeType;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;

class AttributeConstraint {
    private static final String CONSTRAINT_CACHE_KEY = "CONSTRAINT_CACHE";
    
    private String mAttrName;
    private Set<String> mValues;
    
    AttributeConstraint(String attrName) {
        mAttrName = attrName;
    }
    
    String getAttrName() {
        return mAttrName;
    }
    
    void setMin(String min) throws ServiceException {
        throw ServiceException.PARSE_ERROR("min constraint not supported for the attribute type", null);
    }
    
    void setMax(String max) throws ServiceException {
        throw ServiceException.PARSE_ERROR("max constraint not supported for attribute type", null);
    }
    
    void addValue(String value) {
        if (mValues == null)
            mValues = new HashSet<String>();
        mValues.add(value);
    }
    
    boolean violateMinMax(String value) throws ServiceException {
        return false;
    }
    
    boolean violateValues(String value) {
        return (mValues != null && !mValues.contains(value));
    }
    
    boolean violated(Object value) throws ServiceException {
        if (value instanceof String) {
            if (violateValues((String)value))
                return true;
            if (violateMinMax((String)value))
                return true;
            
        } else if (value instanceof String[]) {
            for (String v : (String[])value) {
                if (violateValues(v))
                    return true;
                if (violateMinMax(v))
                    return true;
            }
        } else {
            throw ServiceException.FAILURE("internal error", null); // should not happen
        }
        
        return false;
    }
    
    /*
    boolean.....TRUE|FALSE
    duration....^\d+[hmsd]?$.  If [hmsd] is not specified, the default
                is seconds.
    gentime.....time expressed as \d{14}[zZ]
    enum........value attr is comma-separated list of valid values
    email.......valid email address. must have a "@" and no personal
                part.
    emailp......valid email address. must have a "@" and personal part
                is optional.
    id..........^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$
    integer.....32 bit signed, min/max checked
    port........0-65535
    regex.......value attr is a regular expression. Should explicitly
                add ^ to front and $ at the end
    string......currently just checks max length if specified
    astring.....IA5 string (almost ascii)
    cstring.....case sensitive string
    ostring.....octet string defined in LDAP
    */
    
    private static class IntegerConstraint extends AttributeConstraint {
        private Integer mMin;
        private Integer mMax;
        
        IntegerConstraint(String attrName) {
            super(attrName);
        }
        
        void setMin(String min) {
            try {
                mMin = Integer.valueOf(min);
            } catch (NumberFormatException e) {
                // bad constraint, treat it as there is no min constraint  TODO: log?
            }
        }
        
        void setMax(String max) {
            try {
                mMax = Integer.valueOf(max);
            } catch (NumberFormatException e) {
                // bad constraint, treat it as there is no max constraint TODO: log?
            }
        }
        
        boolean violateMinMax(String valueStr) throws ServiceException {
            try {
                Integer value = Integer.valueOf(valueStr);
                if (mMin != null && value < mMin)
                    return true;
                
                if (mMax != null && value > mMax)
                    return true;
            } catch (NumberFormatException e) { 
                return true; // not a valid integer, bad
            }
            return false;
        }
    }
    
    private static class LongConstraint extends AttributeConstraint {
        private Long mMin;
        private Long mMax;
        
        LongConstraint(String attrName) {
            super(attrName);
        }
        
        void setMin(String min) {
            try {
                mMin = Long.valueOf(min);
            } catch (NumberFormatException e) {
                // bad constraint, treat it as there is no min constraint  TODO: log?
            }
        }
        
        void setMax(String max) {
            try {
                mMax = Long.valueOf(max);
            } catch (NumberFormatException e) {
                // bad constraint, treat it as there is no max constraint TODO: log?
            }
        }
        
        boolean violateMinMax(String valueStr) throws ServiceException {
            try {
                Long value = Long.valueOf(valueStr);
                if (mMin != null && value < mMin)
                    return true;
                
                if (mMax != null && value > mMax)
                    return true;
            } catch (NumberFormatException e) { 
                return true; // not a valid integer, bad
            }
            return false;
        }
    }
    
    private static class DurationConstraint extends AttributeConstraint {
        private Long mMin;
        private Long mMax;
        
        DurationConstraint(String attrName) {
            super(attrName);
        }
        
        void setMin(String min) {
            try {
                mMin = DateUtil.getTimeInterval(min);
            } catch (ServiceException e) {
                // bad constraint, treat it as there is no min constraint TODO: log?
            }
        }
        
        void setMax(String max) {
            try {
                mMax = DateUtil.getTimeInterval(max);
            } catch (ServiceException e) {
                // bad constraint, treat it as there is no max constraint TODO: log?
            }
        }
        
        boolean violateMinMax(String valueStr) throws ServiceException {
            try {
                Long value = DateUtil.getTimeInterval(valueStr);
                if (mMin != null && value < mMin)
                    return true;
                
                if (mMax != null && value > mMax)
                    return true;
            } catch (ServiceException e) { 
                return true; // not a valid interval, bad
            }
            return false;
        }
    }
    
    private static class GentimeConstraint extends AttributeConstraint {
        // milliseconds since January 1, 1970, 00:00:00 GMT represented by this Date object. 
        private Long mMin;  
        private Long mMax;
        
        GentimeConstraint(String attrName) {
            super(attrName);
        }
        
        void setMin(String min) {
            // if cannot parse null will be returned, => no min constraint
            Date date = DateUtil.parseGeneralizedTime(min);
            if (date != null)
                mMin = date.getTime();
        }
        
        void setMax(String max) {
            // if cannot parse null will be returned, => no max constraint
            Date date = DateUtil.parseGeneralizedTime(max);
            if (date != null)
                mMax = date.getTime();
        }
        
        boolean violateMinMax(String valueStr) throws ServiceException {
            Date value = DateUtil.parseGeneralizedTime(valueStr);
            if (value == null)
                return true; // not a valid gentime, bad
            
            long mSecs = value.getTime();    
            if (mMin != null && mSecs < mMin)
                return true;
                
            if (mMax != null && mSecs > mMax)
                return true;
        
            return false;
        }
    }
    
    private static AttributeConstraint fromString(AttributeManager am, String s) throws ServiceException  {
        String[] parts = s.split(":");
        if (parts.length < 2)
            throw ServiceException.PARSE_ERROR("invalid constraint: " + s, null);
            
        String attrName = parts[0];
        AttributeConstraint constraint = newConstratint(am, attrName);
        
        for (int i=1; i<parts.length; i++) {
            String part = parts[i];
            if (part.startsWith("min="))
                constraint.setMin(part.substring(4));
            else if (part.startsWith("max="))
                constraint.setMax(part.substring(4));
            else if (part.startsWith("values=")) {
                String values = part.substring(7);
                String[] vs = values.split(",");
                for (String v : vs)
                    constraint.addValue(v);
            }
        }
        
        return constraint;
    }
        
    private static AttributeConstraint newConstratint(AttributeManager am, String attrName) throws ServiceException {
        AttributeType at = am.getAttributeType(attrName);
        
        switch (at) {
        case TYPE_BOOLEAN:
            return new AttributeConstraint(attrName);
        case TYPE_DURATION:
            return new DurationConstraint(attrName);
        case TYPE_GENTIME:
            return new GentimeConstraint(attrName);
        case TYPE_EMAIL:
        case TYPE_EMAILP:
        case TYPE_ENUM:
        case TYPE_ID:
            return new AttributeConstraint(attrName);
        case TYPE_INTEGER:
            return new IntegerConstraint(attrName);
        case TYPE_PORT:
            return new IntegerConstraint(attrName);
        case TYPE_PHONE:
        case TYPE_STRING:
        case TYPE_ASTRING:
        case TYPE_OSTRING:
        case TYPE_CSTRING:
        case TYPE_REGEX:
            return new AttributeConstraint(attrName);
        case TYPE_LONG:
            return new LongConstraint(attrName);
        }
        
        throw ServiceException.FAILURE("internal error", null);
    }
    
    static Entry getConstraintEntry(Entry entry) throws ServiceException {
        Provisioning prov = Provisioning.getInstance();
        Entry constraintEntry = null;
        
        if (entry instanceof Account)
            constraintEntry = prov.getCOS((Account)entry);
        else if (entry instanceof Domain || entry instanceof Server)
            constraintEntry = prov.getConfig();
        
        return constraintEntry;
    }
    
    static Map<String, AttributeConstraint> getConstraint(Entry constraintEntry) throws ServiceException {
        
        Map<String, AttributeConstraint> constraints = (Map<String, AttributeConstraint>)constraintEntry.getCachedData(CONSTRAINT_CACHE_KEY);
        if (constraints == null) {
            constraints = new HashMap<String, AttributeConstraint>();
            
            //
            // if there is no zimbraConstraint, we get an empty Set from getMultiAttrSet
            // and we will cache and return an empty set of AttributeConstraint.  
            // This is good because we do not want to repeatedly read LDAP if zimbraConstraint
            // is not set
            //
            Set<String> cstrnts = constraintEntry.getMultiAttrSet(Provisioning.A_zimbraConstraint);
            
            AttributeManager am = AttributeManager.getInstance();
            for (String c : cstrnts) {
                AttributeConstraint constraint = AttributeConstraint.fromString(am, c);
                constraints.put(constraint.getAttrName(), constraint);
            }
            
            constraintEntry.setCachedData(CONSTRAINT_CACHE_KEY, constraints);
        }
        
        return constraints;
    }

    static boolean violateConstraint(Map<String, AttributeConstraint> constraints, String attrName, Object value) throws ServiceException {
        AttributeConstraint constraint = constraints.get(attrName);
        if (constraint == null)
            return false;
        
        return constraint.violated(value);  
    }
    
    
    private static void test(Map<String, AttributeConstraint> constraints, String attrName, Object value, boolean expected) throws ServiceException {
        boolean violated = violateConstraint(constraints, attrName, value);
        StringBuilder sb = new StringBuilder();
        if (value instanceof String[]) {
            for (String s : (String[])value)
                sb.append(s + " ");
        } else
            sb.append(value.toString());
            
            
        System.out.println("Setting " + attrName + " to " + "[" + sb.toString() + "]" + " => " + (violated?"denied":"allowed"));
        if (violated != expected)
            System.out.println("failed\n");
    }
    /**
     * @param args
     */
    public static void main(String[] args) throws ServiceException  {
        Provisioning prov = Provisioning.getInstance();
        AttributeManager am = AttributeManager.getInstance();
        
        AttributeConstraint.fromString(am, "zimbraPasswordMinLength:min=6");
        AttributeConstraint.fromString(am, "zimbraPasswordMaxLength:min=64");
        AttributeConstraint.fromString(am, "zimbraPasswordMinLength:min=6:max=64:vaues=1,2,3");
        AttributeConstraint.fromString(am, "zimbraFeatureMailEnabled:values=FALSE,TRUE");
        
        Account acct = prov.get(Provisioning.AccountBy.name, "user1@phoebe.mac");
        Cos cos = prov.getCOS(acct);
        Map<String, Object> cosConstraints = new HashMap<String,Object>();
        
        // integer
        cos.addConstraint("zimbraPasswordMinLength:min=6:max=10:values=8,9", cosConstraints);
        
        // long
        long longMax = Long.MAX_VALUE;
        long longMaxMinusOne = longMax - 1;
        cos.addConstraint("zimbraMailQuota:max="+longMaxMinusOne, cosConstraints);
        
        // duration
        cos.addConstraint("zimbraPasswordLockoutDuration:min=5h:max=1d", cosConstraints);
        
        // gentime
        cos.addConstraint("zimbraPrefPop3DownloadSince:min=20060315023000Z", cosConstraints);
        
        // string
        cos.addConstraint("zimbraPrefGroupMailBy:values=conversation", cosConstraints);
        
        // bad constraint, invalid min constraint
        cos.addConstraint("zimbraCalendarMaxRevisions:min=zz:max=10", cosConstraints);
        
        // multi-value
        cos.addConstraint("zimbraZimletAvailableZimlets:values=A,B,C", cosConstraints);
        
        
        prov.modifyAttrs(cos, cosConstraints);
        
        Map<String, AttributeConstraint> constraints = getConstraint(acct);
        // integer
        test(constraints, "zimbraPasswordMinLength", "5", true);
        test(constraints, "zimbraPasswordMinLength", "6", true);
        test(constraints, "zimbraPasswordMinLength", "7", true);
        test(constraints, "zimbraPasswordMinLength", "8", false);
        test(constraints, "zimbraPasswordMinLength", "9", false);
        test(constraints, "zimbraPasswordMinLength", "10", true);
        test(constraints, "zimbraPasswordMinLength", "11", true);
        
        // long 
        test(constraints, "zimbraMailQuota", "" + longMaxMinusOne, false);
        test(constraints, "zimbraMailQuota", "" + longMax, true);
        
        // duration
        test(constraints, "zimbraPasswordLockoutDuration", "3h", true);
        test(constraints, "zimbraPasswordLockoutDuration", "25h", true);
        test(constraints, "zimbraPasswordLockoutDuration", "30m", true);
        test(constraints, "zimbraPasswordLockoutDuration", "5h", false);
        test(constraints, "zimbraPasswordLockoutDuration", "600m", false);
        test(constraints, "zimbraPasswordLockoutDuration", "24h", false);
        
        // gentime 
        test(constraints, "zimbraPrefPop3DownloadSince", "20050315023000Z", true);
        test(constraints, "zimbraPrefPop3DownloadSince", "20060315023000Z", false);
        test(constraints, "zimbraPrefPop3DownloadSince", "20060315023001Z", false);
        
        // string, enum, ...
        test(constraints, "zimbraPrefGroupMailBy", "message", true);
        test(constraints, "zimbraPrefGroupMailBy", "conversation", false);
        
        // bad constraint
        test(constraints, "zimbraCalendarMaxRevisions", "1", false);
        test(constraints, "zimbraCalendarMaxRevisions", "10", false);
        test(constraints, "zimbraCalendarMaxRevisions", "11", true);
        
        // multi-value
        test(constraints, "zimbraZimletAvailableZimlets", new String[]{"A","B"}, false);
        test(constraints, "zimbraZimletAvailableZimlets", new String[]{"A","X"}, true);
        
        test(constraints, "zimbraPasswordMaxLength", "100", false); // no constraint
    }

}
