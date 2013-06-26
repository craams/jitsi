/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.addrbook.msoutlook;

import java.util.*;
import java.util.regex.*;

import net.java.sip.communicator.plugin.addrbook.*;
import net.java.sip.communicator.service.contactsource.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

/**
 * Implements <tt>ContactQuery</tt> for the Address Book of Microsoft Outlook.
 *
 * @author Lyubomir Marinov
 * @author Vincent Lucas
 */
public class MsOutlookAddrBookContactQuery
    extends AbstractAddrBookContactQuery<MsOutlookAddrBookContactSourceService>
{
    /**
     * The <tt>Logger</tt> used by the <tt>MsOutlookAddrBookContactQuery</tt>
     * class and its instances for logging output.
     */
    private static final Logger logger
        = Logger.getLogger(MsOutlookAddrBookContactQuery.class);

    private static final int dispidEmail1EmailAddress = 12;

    private static final int dispidEmail2EmailAddress = 13;

    private static final int dispidEmail3EmailAddress = 14;

    /**
     * The object type of a <tt>SourceContact</tt> in the Address Book of
     * Microsoft Outlook.
     */
    private static final long MAPI_MAILUSER = 0x00000006;

    /**
     * The IDs of the properties of <tt>MAPI_MAILUSER</tt> which are to be
     * queried by the <tt>MsOutlookAddrBookContactQuery</tt> instances.
     */
    public static final long[] MAPI_MAILUSER_PROP_IDS
        = new long[]
        {
            0x3001 /* PR_DISPLAY_NAME */,
            0x3003 /* PR_EMAIL_ADDRESS */,
            0x3A06 /* PR_GIVEN_NAME */,
            0x3A44 /* PR_MIDDLE_NAME */,
            0x3A11 /* PR_SURNAME */,
            0x3A08 /* PR_BUSINESS_TELEPHONE_NUMBER */,
            0x3A1B /* PR_BUSINESS2_TELEPHONE_NUMBER */,
            0x3A09 /* PR_HOME_TELEPHONE_NUMBER */,
            0x3A2F /* PR_HOME2_TELEPHONE_NUMBER */,
            0x3A1C /* PR_MOBILE_TELEPHONE_NUMBER */,
            0x3A1F /* PR_OTHER_TELEPHONE_NUMBER */,
            0x0FFE /* PR_OBJECT_TYPE */,
            0x00008083 /* dispidEmail1EmailAddress */,
            0x00008093 /* dispidEmail2EmailAddress */,
            0x000080A3 /* dispidEmail3EmailAddress */,
            0x3A16 /* PR_COMPANY_NAME */,
            0x0FFF /* PR_ORIGINAL_ENTRYID */,
            0x3A24 /* dispidFax1EmailAddress */,
            0x3A25 /* dispidFax2EmailAddress */,
            0x3A23 /* dispidFax3EmailAddress */,
            0x3A4F /* PR_NICKNAME */,
            0x3A45 /* PR_DISPLAY_NAME_PREFIX */,
            0x3A50 /* PR_PERSONAL_HOME_PAGE */,
            0x3A51 /* PR_BUSINESS_HOME_PAGE */,
            0x3A17 /* PR_TITLE */,
            0x00008062 /* dispidInstMsg */,
            0x00008046, // PR_BUSINESS_ADDRESS_CITY
            0x00008049, // PR_BUSINESS_ADDRESS_COUNTRY
            0x00008048, // PR_BUSINESS_ADDRESS_POSTAL_CODE
            0x00008047, // PR_BUSINESS_ADDRESS_STATE_OR_PROVINCE
            0x00008045, // PR_BUSINESS_ADDRESS_STREET
            0x3A59, // PR_HOME_ADDRESS_CITY
            0x3A5A, // PR_HOME_ADDRESS_COUNTRY
            0x3A5B, // PR_HOME_ADDRESS_POSTAL_CODE
            0x3A5C, // PR_HOME_ADDRESS_STATE_OR_PROVINCE
            0x3A5D // PR_HOME_ADDRESS_STREET
        };

    /**
     * The object type of a <tt>SourceContact</tt> in a Contacts folder of
     * Microsoft Outlook.
     */
    private static final long MAPI_MESSAGE = 0x00000005;

    /**
     * The flag which signals that MAPI strings should be returned in the
     * Unicode character set.
     */
    public static final long MAPI_UNICODE = 0x80000000;

    /**
     * The id of the <tt>PR_ATTACHMENT_CONTACTPHOTO</tt> MAPI property.
     */
    private static final long PR_ATTACHMENT_CONTACTPHOTO = 0x7FFF;

    /**
     * The index of the id of the <tt>PR_BUSINESS_TELEPHONE_NUMBER</tt> property
     * in {@link #MAPI_MAILUSER_PROP_IDS}.
     */
    private static final int PR_BUSINESS_TELEPHONE_NUMBER = 5;

    /**
     * The index of the id of the <tt>PR_BUSINESS2_TELEPHONE_NUMBER</tt>
     * property in {@link #MAPI_MAILUSER_PROP_IDS}.
     */
    private static final int PR_BUSINESS2_TELEPHONE_NUMBER = 6;

    private static final int PR_COMPANY_NAME = 15;

    /**
     * The index of the id of the <tt>PR_DISPLAY_NAME</tt> property in
     * {@link #MAPI_MAILUSER_PROP_IDS}.
     */
    private static final int PR_DISPLAY_NAME = 0;

    /**
     * The index of the id of the <tt>PR_EMAIL_ADDRESS</tt> property in
     * {@link #MAPI_MAILUSER_PROP_IDS}.
     */
    private static final int PR_EMAIL_ADDRESS = 1;

    /**
     * The index of the id of the <tt>PR_GIVEN_NAME</tt> property in
     * {@link #MAPI_MAILUSER_PROP_IDS}.
     */
    private static final int PR_GIVEN_NAME = 2;

    /**
     * The index of the id of the <tt>PR_HOME_TELEPHONE_NUMBER</tt> property in
     * {@link #MAPI_MAILUSER_PROP_IDS}.
     */
    private static final int PR_HOME_TELEPHONE_NUMBER = 7;

    /**
     * The index of the id of the <tt>PR_HOME2_TELEPHONE_NUMBER</tt> property in
     * {@link #MAPI_MAILUSER_PROP_IDS}.
     */
    private static final int PR_HOME2_TELEPHONE_NUMBER = 8;

    /**
     * The index of the id of the <tt>PR_MIDDLE_NAME</tt> property in
     * {@link #MAPI_MAILUSER_PROP_IDS}.
     */
    private static final int PR_MIDDLE_NAME = 3;

    /**
     * The index of the id of the <tt>PR_MOBILE_TELEPHONE_NUMBER</tt> property
     * in {@link #MAPI_MAILUSER_PROP_IDS}.
     */
    private static final int PR_MOBILE_TELEPHONE_NUMBER = 9;

    /**
     * The index of the id of the <tt>PR_OTHER_TELEPHONE_NUMBER</tt> property
     * in {@link #MAPI_MAILUSER_PROP_IDS}.
     */
    private static final int PR_OTHER_TELEPHONE_NUMBER = 10;

    /**
     * The index of the id of the <tt>PR_OBJECT_TYPE</tt> property in
     * {@link #MAPI_MAILUSER_PROP_IDS}.
     */
    private static final int PR_OBJECT_TYPE = 11;

    /**
     * The index of the id of the <tt>PR_SURNAME</tt> property in
     * {@link #MAPI_MAILUSER_PROP_IDS}.
     */
    private static final int PR_SURNAME = 4;

    /**
     * The index of the id of the <tt>PR_ORIGINAL_ENTRYID</tt> property
     * in {@link #MAPI_MAILUSER_PROP_IDS}.
     */
    private static final int PR_ORIGINAL_ENTRYID = 16;

    /**
     * The index of the 1st fax telephone number (business fax).
     */
    private static final int dispidFax1EmailAddress = 17;

    /**
     * The index of the 2nd fax telephone number (home fax).
     */
    private static final int dispidFax2EmailAddress = 18;

    /**
     * The index of the 3rd fax telephone number (other fax).
     */
    private static final int dispidFax3EmailAddress = 19;

    /**
     * The index of the nickname.
     */
    private static final int PR_NICKNAME = 20;

    /**
     * The index of the name prefix.
     */
    private static final int PR_DISPLAY_NAME_PREFIX = 21;

    /**
     * The index of the personnal home page
     */
    private static final int PR_PERSONAL_HOME_PAGE = 22;

    /**
     * The index of the business home page
     */
    private static final int PR_BUSINESS_HOME_PAGE = 23;

    /**
     * The index of the job title.
     */
    private static final int PR_TITLE = 24;

    /**
     * The index of the instant messaging address.
     */
    private static final int dispidInstMsg = 25;

    /**
     * The index of the business city of the postal address.
     */
    private static final int PR_BUSINESS_ADDRESS_CITY = 26;

    /**
     * The index of the business country of the postal address.
     */
    private static final int PR_BUSINESS_ADDRESS_COUNTRY = 27;

    /**
     * The index of the business postal code of the postal address.
     */
    private static final int PR_BUSINESS_ADDRESS_POSTAL_CODE = 28;

    /**
     * The index of the business state or province of the postal address.
     */
    private static final int PR_BUSINESS_ADDRESS_STATE_OR_PROVINCE = 29;

    /**
     * The index of the business street of the postal address.
     */
    private static final int PR_BUSINESS_ADDRESS_STREET = 30;

    /**
     * The index of the home city of the postal address.
     */
    private static final int PR_HOME_ADDRESS_CITY = 31;

    /**
     * The index of the home country of the postal address.
     */
    private static final int PR_HOME_ADDRESS_COUNTRY = 32;

    /**
     * The index of the home postal code of the postal address.
     */
    private static final int PR_HOME_ADDRESS_POSTAL_CODE = 33;

    /**
     * The index of the home state or province of the postal address.
     */
    private static final int PR_HOME_ADDRESS_STATE_OR_PROVINCE = 34;

    /**
     * The index of the home street of the postal address.
     */
    private static final int PR_HOME_ADDRESS_STREET = 35;

    /**
     * The indexes in {@link #MAPI_MAILUSER_PROP_IDS} of the property IDs which
     * are to be represented in <tt>SourceContact</tt> as
     * <tt>ContactDetail</tt>s.
     */
    private static final int[] CONTACT_DETAIL_PROP_INDEXES
        = new int[]
        {
            PR_EMAIL_ADDRESS,
            PR_GIVEN_NAME,
            PR_MIDDLE_NAME,
            PR_SURNAME,
            PR_BUSINESS_TELEPHONE_NUMBER,
            PR_BUSINESS2_TELEPHONE_NUMBER,
            PR_HOME_TELEPHONE_NUMBER,
            PR_HOME2_TELEPHONE_NUMBER,
            PR_MOBILE_TELEPHONE_NUMBER,
            PR_OTHER_TELEPHONE_NUMBER,
            dispidEmail1EmailAddress,
            dispidEmail2EmailAddress,
            dispidEmail3EmailAddress,
            PR_COMPANY_NAME,
            dispidFax1EmailAddress,
            dispidFax2EmailAddress,
            dispidFax3EmailAddress,
            PR_NICKNAME,
            PR_DISPLAY_NAME_PREFIX,
            PR_PERSONAL_HOME_PAGE,
            PR_BUSINESS_HOME_PAGE,
            PR_TITLE,
            dispidInstMsg,
            PR_BUSINESS_ADDRESS_CITY,
            PR_BUSINESS_ADDRESS_COUNTRY,
            PR_BUSINESS_ADDRESS_POSTAL_CODE,
            PR_BUSINESS_ADDRESS_STATE_OR_PROVINCE,
            PR_BUSINESS_ADDRESS_STREET,
            PR_HOME_ADDRESS_CITY,
            PR_HOME_ADDRESS_COUNTRY,
            PR_HOME_ADDRESS_POSTAL_CODE,
            PR_HOME_ADDRESS_STATE_OR_PROVINCE,
            PR_HOME_ADDRESS_STREET
        };

    /**
     * The indexes in {@link #MAPI_MAILUSER_PROP_IDS} of the property IDs which
     * represent an identifier which can be used for telephony or persistent
     * presence.
     */
    private static final int[] CONTACT_OPERATION_SET_ABLE_PROP_INDEXES
        = new int[]
        {
            PR_EMAIL_ADDRESS,
            PR_BUSINESS_TELEPHONE_NUMBER,
            PR_BUSINESS2_TELEPHONE_NUMBER,
            PR_HOME_TELEPHONE_NUMBER,
            PR_HOME2_TELEPHONE_NUMBER,
            PR_MOBILE_TELEPHONE_NUMBER,
            PR_OTHER_TELEPHONE_NUMBER,
            dispidEmail1EmailAddress,
            dispidEmail2EmailAddress,
            dispidEmail3EmailAddress,
            dispidFax1EmailAddress,
            dispidFax2EmailAddress,
            dispidFax3EmailAddress,
            dispidInstMsg
        };

    static
    {
        System.loadLibrary("jmsoutlookaddrbook");
    }

    /**
     * The number of <tt>SourceContact</tt>s matching this <tt>ContactQuery</tt>
     * which have been retrieved from Contacts folders. Since each one of them
     * may appear multiple times in the Address Book as well, no matching in the
     * Address Book will be performed if there is at least one matching
     * <tt>SourceContact</tt> in a Contacts folder.
     */
    private int mapiMessageCount;

    /**
     * Boolea used to defined if we already get and logged a read contact
     * property error. 
     */
    private boolean firstIMAPIPropGetPropFailureLogged = false;

    /**
     * Initializes a new <tt>MsOutlookAddrBookContactQuery</tt> instance to
     * be performed by a specific
     * <tt>MsOutlookAddrBookContactSourceService</tt>.
     *
     * @param msoabcss the <tt>MsOutlookAddrBookContactSourceService</tt>
     * which is to perform the new <tt>ContactQuery</tt>
     * @param query the <tt>Pattern</tt> for which <tt>msoabcss</tt> is being
     * queried
     */
    public MsOutlookAddrBookContactQuery(
            MsOutlookAddrBookContactSourceService msoabcss,
            Pattern query)
    {
        super(msoabcss, query);
        if(logger.isDebugEnabled())
        {
            logger.debug("Creating new query: " + query.toString());
        }
    }

    /**
     * Calls back to a specific <tt>PtrCallback</tt> for each
     * <tt>MAPI_MAILUSER</tt> found in the Address Book of Microsoft Outlook
     * which matches a specific <tt>String</tt> query.
     *
     * @param query the <tt>String</tt> for which the Address Book of Microsoft
     * Outlook is to be queried. <b>Warning</b>: Ignored at the time of this
     * writing.
     * @param callback the <tt>PtrOutlookContactCallback</tt> to be notified
     * about the matching <tt>MAPI_MAILUSER</tt>s
     */
    public static native void foreachMailUser(
            String query,
            PtrOutlookContactCallback callback);

    private static ContactDetail.Category getCategory(int propIndex)
    {
        switch (propIndex)
        {
        case PR_GIVEN_NAME:
        case PR_MIDDLE_NAME:
        case PR_SURNAME:
        case PR_NICKNAME:
        case PR_DISPLAY_NAME_PREFIX:
        case PR_PERSONAL_HOME_PAGE:
            return ContactDetail.Category.Personal;
        case PR_COMPANY_NAME:
        case PR_BUSINESS_HOME_PAGE:
        case PR_TITLE:
            return ContactDetail.Category.Organization;
        case dispidEmail1EmailAddress:
        case dispidEmail2EmailAddress:
        case dispidEmail3EmailAddress:
        case PR_EMAIL_ADDRESS:
            return ContactDetail.Category.Email;
        case PR_BUSINESS2_TELEPHONE_NUMBER:
        case PR_BUSINESS_TELEPHONE_NUMBER:
        case PR_HOME2_TELEPHONE_NUMBER:
        case PR_HOME_TELEPHONE_NUMBER:
        case PR_MOBILE_TELEPHONE_NUMBER:
        case PR_OTHER_TELEPHONE_NUMBER:
        case dispidFax1EmailAddress:
        case dispidFax2EmailAddress:
        case dispidFax3EmailAddress:
            return ContactDetail.Category.Phone;
        case dispidInstMsg:
            return ContactDetail.Category.InstantMessaging;
        case PR_BUSINESS_ADDRESS_CITY:
        case PR_BUSINESS_ADDRESS_COUNTRY:
        case PR_BUSINESS_ADDRESS_POSTAL_CODE:
        case PR_BUSINESS_ADDRESS_STATE_OR_PROVINCE:
        case PR_BUSINESS_ADDRESS_STREET:
        case PR_HOME_ADDRESS_CITY:
        case PR_HOME_ADDRESS_COUNTRY:
        case PR_HOME_ADDRESS_POSTAL_CODE:
        case PR_HOME_ADDRESS_STATE_OR_PROVINCE:
        case PR_HOME_ADDRESS_STREET:
            return ContactDetail.Category.Address;
        default:
            return null;
        }
    }

    /**
     * Gets the set of <tt>ContactDetail</tt> labels to be assigned to a
     * property specified by its index in {@link #MAPI_MAILUSER_PROP_IDS}.
     *
     * @param propIndex the index in <tt>MAPI_MAILUSER_PROP_IDS</tt> of the
     * property to get the <tt>ContactDetail</tt> labels of
     * @return the set of <tt>ContactDetail</tt> labels to be assigned to the
     * property specified by its index in <tt>MAPI_MAILUSER_PROP_IDS</tt>
     */
    private static ContactDetail.SubCategory[] getSubCategories(int propIndex)
    {
        switch (propIndex)
        {
        case PR_GIVEN_NAME:
        case PR_MIDDLE_NAME:
        case PR_COMPANY_NAME:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.Name
                        };
        case PR_SURNAME:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.LastName
                        };
        case PR_NICKNAME:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.Nickname
                        };
        case PR_BUSINESS2_TELEPHONE_NUMBER:
        case PR_BUSINESS_TELEPHONE_NUMBER:
        case dispidEmail2EmailAddress:
        case PR_EMAIL_ADDRESS:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.Work
                        };
        case PR_HOME2_TELEPHONE_NUMBER:
        case PR_HOME_TELEPHONE_NUMBER:
        case dispidEmail1EmailAddress:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.Home
                        };
        case PR_MOBILE_TELEPHONE_NUMBER:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.Mobile
                        };
        case PR_OTHER_TELEPHONE_NUMBER:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.Other
                        };
        case dispidFax1EmailAddress:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.Fax,
                        };
        case dispidEmail3EmailAddress:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.Other
                        };
        case PR_TITLE:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.JobTitle
                        };
        case PR_BUSINESS_ADDRESS_CITY:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.Work,
                            ContactDetail.SubCategory.City
                        };
        case PR_BUSINESS_ADDRESS_COUNTRY:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.Work,
                            ContactDetail.SubCategory.Country
                        };
        case PR_BUSINESS_ADDRESS_POSTAL_CODE:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.Work,
                            ContactDetail.SubCategory.PostalCode
                        };
        case PR_BUSINESS_ADDRESS_STATE_OR_PROVINCE:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.Work,
                            ContactDetail.SubCategory.State
                        };
        case PR_BUSINESS_ADDRESS_STREET:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.Work,
                            ContactDetail.SubCategory.Street
                        };
        case PR_HOME_ADDRESS_CITY:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.Home,
                            ContactDetail.SubCategory.City
                        };
        case PR_HOME_ADDRESS_COUNTRY:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.Home,
                            ContactDetail.SubCategory.Country
                        };
        case PR_HOME_ADDRESS_POSTAL_CODE:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.Home,
                            ContactDetail.SubCategory.PostalCode
                        };
        case PR_HOME_ADDRESS_STATE_OR_PROVINCE:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.Home,
                            ContactDetail.SubCategory.State
                        };
        case PR_HOME_ADDRESS_STREET:
            return
                new ContactDetail.SubCategory[]
                        {
                            ContactDetail.SubCategory.Home,
                            ContactDetail.SubCategory.Street
                        };
        default:
            return null;
        }
    }

    /**
     * Find the outlook property tag from category and subcategories.
     *
     * @param category The category.
     * @param subCategories The subcategories.
     *
     * @return The outlook property tag corresponding to the given category and
     * subcategories.
     */
    public static long getProperty(
        ContactDetail.Category category,
        Collection<ContactDetail.SubCategory> subCategories)
    {
        switch(category)
        {
        case Personal:
            if(subCategories.contains(ContactDetail.SubCategory.Name))
                return MAPI_MAILUSER_PROP_IDS[PR_GIVEN_NAME];
            else if(subCategories.contains(
                        ContactDetail.SubCategory.LastName))
                return MAPI_MAILUSER_PROP_IDS[PR_SURNAME];
            else if(subCategories.contains(
                        ContactDetail.SubCategory.Nickname))
                return MAPI_MAILUSER_PROP_IDS[PR_NICKNAME];
            else if(subCategories.contains(
                        ContactDetail.SubCategory.HomePage))
                return MAPI_MAILUSER_PROP_IDS[PR_PERSONAL_HOME_PAGE];
            else
                return MAPI_MAILUSER_PROP_IDS[PR_DISPLAY_NAME_PREFIX];
        case Organization:
            if(subCategories.contains(ContactDetail.SubCategory.Name))
                return MAPI_MAILUSER_PROP_IDS[PR_COMPANY_NAME];
            else if(subCategories.contains(ContactDetail.SubCategory.JobTitle))
                return MAPI_MAILUSER_PROP_IDS[PR_TITLE];
            else
                return MAPI_MAILUSER_PROP_IDS[PR_BUSINESS_HOME_PAGE];
        case Email:
            if(subCategories.contains(ContactDetail.SubCategory.Work))
                return MAPI_MAILUSER_PROP_IDS[dispidEmail2EmailAddress];
            else if(subCategories.contains(
                        ContactDetail.SubCategory.Home))
                return MAPI_MAILUSER_PROP_IDS[dispidEmail1EmailAddress];
            else if(subCategories.contains(
                        ContactDetail.SubCategory.Other))
                return MAPI_MAILUSER_PROP_IDS[dispidEmail3EmailAddress];
            break;
        case Phone:
            if(subCategories.contains(ContactDetail.SubCategory.Fax))
                return MAPI_MAILUSER_PROP_IDS[dispidFax1EmailAddress];
            else if(subCategories.contains(ContactDetail.SubCategory.Work))
                return MAPI_MAILUSER_PROP_IDS[PR_BUSINESS_TELEPHONE_NUMBER];
            else if(subCategories.contains(ContactDetail.SubCategory.Home))
                return MAPI_MAILUSER_PROP_IDS[PR_HOME_TELEPHONE_NUMBER];
            else if(subCategories.contains(
                        ContactDetail.SubCategory.Mobile))
                return MAPI_MAILUSER_PROP_IDS[PR_MOBILE_TELEPHONE_NUMBER];
            else if(subCategories.contains(
                        ContactDetail.SubCategory.Other))
                return MAPI_MAILUSER_PROP_IDS[PR_OTHER_TELEPHONE_NUMBER];
            break;
        case InstantMessaging:
            return MAPI_MAILUSER_PROP_IDS[dispidInstMsg];
        case Address:
            if(subCategories.contains(ContactDetail.SubCategory.Work))
            {
                if(subCategories.contains(ContactDetail.SubCategory.City))
                {
                    return MAPI_MAILUSER_PROP_IDS[PR_BUSINESS_ADDRESS_CITY];
                }
                else if(subCategories.contains(
                            ContactDetail.SubCategory.Country))
                {
                    return MAPI_MAILUSER_PROP_IDS[PR_BUSINESS_ADDRESS_COUNTRY];
                }
                else if(subCategories.contains(
                            ContactDetail.SubCategory.PostalCode))
                {
                    return MAPI_MAILUSER_PROP_IDS[
                        PR_BUSINESS_ADDRESS_POSTAL_CODE];
                }
                else if(subCategories.contains(ContactDetail.SubCategory.State))
                {
                    return MAPI_MAILUSER_PROP_IDS[
                        PR_BUSINESS_ADDRESS_STATE_OR_PROVINCE];
                }
                else if(subCategories.contains(
                            ContactDetail.SubCategory.Street))
                {
                    return MAPI_MAILUSER_PROP_IDS[PR_BUSINESS_ADDRESS_STREET];
                }
            }
            else if(subCategories.contains(ContactDetail.SubCategory.Home))
            {
                if(subCategories.contains(ContactDetail.SubCategory.City))
                {
                    return MAPI_MAILUSER_PROP_IDS[PR_HOME_ADDRESS_CITY];
                }
                else if(subCategories.contains(
                            ContactDetail.SubCategory.Country))
                {
                    return MAPI_MAILUSER_PROP_IDS[PR_HOME_ADDRESS_COUNTRY];
                }
                else if(subCategories.contains(
                            ContactDetail.SubCategory.PostalCode))
                {
                    return MAPI_MAILUSER_PROP_IDS[PR_HOME_ADDRESS_POSTAL_CODE];
                }
                else if(subCategories.contains(ContactDetail.SubCategory.State))
                {
                    return MAPI_MAILUSER_PROP_IDS[
                        PR_HOME_ADDRESS_STATE_OR_PROVINCE];
                }
                else if(subCategories.contains(
                            ContactDetail.SubCategory.Street))
                {
                    return MAPI_MAILUSER_PROP_IDS[PR_HOME_ADDRESS_STREET];
                }
            }
        }
        return -1;
    }

    public static native Object[] IMAPIProp_GetProps(
            String entryId,
            long[] propIds, long flags)
        throws MsOutlookMAPIHResultException;

    public static native boolean IMAPIProp_SetPropString(
            long propId,
            String value,
            String entryId);

    public static native boolean IMAPIProp_DeleteProp(
            long propId,
            String entryId);

    /**
     * Removes a contact from the address book.
     *
     * @param id the person id.
     *
     * @return whether the contact was successfully removed.
     */
    public static native boolean deleteContact(String id);

    /**
     * Creates an empty contact from the address book.
     *
     * @return The id of the new contact created. Or NULL if the ceration
     * failed.
     */
    public static native String createContact();

    /**
     * Determines whether a specific index in {@link #MAPI_MAILUSER_PROP_IDS}
     * stands for a property with a phone number value.
     *
     * @param propIndex the index in <tt>MAPI_MAILUSER_PROP_IDS</tt> of the
     * property to check
     * @return <tt>true</tt> if <tt>propIndex</tt> stands for a property with a
     * phone number value; otherwise, <tt>false</tt>
     */
    private static boolean isPhoneNumber(int propIndex)
    {
        switch (propIndex)
        {
        case PR_BUSINESS2_TELEPHONE_NUMBER:
        case PR_BUSINESS_TELEPHONE_NUMBER:
        case PR_HOME2_TELEPHONE_NUMBER:
        case PR_HOME_TELEPHONE_NUMBER:
        case PR_MOBILE_TELEPHONE_NUMBER:
            return true;
        default:
            return false;
        }
    }

    /**
     * Determines whether a specific <tt>MAPI_MAILUSER</tt> property with a
     * specific <tt>value</tt> matches the {@link #query} of this
     * <tt>AsyncContactQuery</tt>.
     *
     * @param property the <tt>MAPI_MAILUSER</tt> property to check
     * @param value the value of the <tt>property</tt> to check
     * @return <tt>true</tt> if the specified <tt>value</tt> of the specified
     * <tt>property</tt> matches the <tt>query</tt> of this
     * <tt>AsyncContactQuery</tt>; otherwise, <tt>false</tt>
     */
    private boolean matches(int property, String value)
    {
        return
            query.matcher(value).find()
                || (isPhoneNumber(property) && phoneNumberMatches(value));
    }

    /**
     * Notifies this <tt>MsOutlookAddrBookContactQuery</tt> about a specific
     * <tt>MAPI_MAILUSER</tt>.
     *
     * @param id The outlook contact identifier.
     *
     * @return <tt>true</tt> if this <tt>MsOutlookAddrBookContactQuery</tt>
     * is to continue being called; otherwise, <tt>false</tt>
     * @throws MsOutlookMAPIHResultException if anything goes wrong while
     * getting the properties of the specified <tt>MAPI_MAILUSER</tt>
     */
    private boolean onMailUser(String id)
        throws MsOutlookMAPIHResultException
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("Found contact id: " + id);
        }

        Object[] props = null;
        try
        {
            props
                = IMAPIProp_GetProps(id, MAPI_MAILUSER_PROP_IDS, MAPI_UNICODE);
        }
        catch(MsOutlookMAPIHResultException ex)
        {
            if(ex.getHresultString().equals("MAPI_E_0x57")
                    && firstIMAPIPropGetPropFailureLogged == false)
            {
                firstIMAPIPropGetPropFailureLogged = true;
                throw ex;
            }
            else if(!ex.getHresultString().equals("MAPI_E_0x57"))
            {
                throw ex;
            }
            return true;
        }

        long objType = 0;
        if(props != null
                && props[PR_OBJECT_TYPE] != null
                && props[PR_OBJECT_TYPE] instanceof Long)
        {
            objType = ((Long) props[PR_OBJECT_TYPE]).longValue();
        }
        else
        {
            return false;
        }

        // If we have results from the Contacts folder(s), don't read from the
        // Address Book because there may be duplicates.
        if ((MAPI_MAILUSER == objType) && (mapiMessageCount != 0))
            return false;

        int propIndex = 0;
        boolean matches = false;

        Object prop;
        for(int i = 0; i < props.length; ++i)
        {
            prop = props[i];
            if ((prop instanceof String)
                    && matches(propIndex, (String) prop)
                    && i != PR_ORIGINAL_ENTRYID)
            {
                matches = true;
                break;
            }
            propIndex++;
        }

        if (matches)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("Contact matches: " + id
                        + ", displayName: " + getDisplayName(props));
            }
            List<ContactDetail> contactDetails = getContactDetails(props);

            // What's the point of showing a contact who has no contact details?
            if (!contactDetails.isEmpty())
            {
                String displayName = getDisplayName(props);

                MsOutlookAddrBookSourceContact sourceContact
                    = new MsOutlookAddrBookSourceContact(
                            getContactSource(),
                            (String) props[PR_ORIGINAL_ENTRYID],
                            displayName,
                            getOrganization(props),
                            contactDetails);

                if (MAPI_MESSAGE == objType)
                {
                    ++mapiMessageCount;

                    try
                    {
                        Object[] images
                            = IMAPIProp_GetProps(
                                    id,
                                    new long[] { PR_ATTACHMENT_CONTACTPHOTO },
                                    0);
                        Object image = images[0];

                        if (image instanceof byte[])
                            sourceContact.setImage((byte[]) image);
                    }
                    catch (MsOutlookMAPIHResultException ex)
                    {
                        // Ignore it, the image isn't as vital as the
                        // SourceContact.
                    }
                }

                addQueryResult(sourceContact);
            }
        }
        return (getStatus() == QUERY_IN_PROGRESS);
    }

    /**
     * Gets the <tt>contactDetails</tt> to be set on a <tt>SourceContact</tt>
     * which is to represent an <tt>ABPerson</tt> specified by the values of its
     * {@link #ABPERSON_PROPERTIES}.
     *
     * @param values the values of the <tt>ABPERSON_PROPERTIES</tt> which
     * represent the <tt>ABPerson</tt> to get the <tt>contactDetails</tt> of
     * @return the <tt>contactDetails</tt> to be set on a <tt>SourceContact</tt>
     * which is to represent the <tt>ABPerson</tt> specified by <tt>values</tt>
     */
    public static List<ContactDetail> getContactDetails(Object[] values)
    {
        List<Class<? extends OperationSet>> supportedOpSets
            = new ArrayList<Class<? extends OperationSet>>(2);
        supportedOpSets.add(OperationSetBasicTelephony.class);
        // can be added as contacts
        supportedOpSets.add(OperationSetPersistentPresence.class);

        List<ContactDetail> contactDetails = new LinkedList<ContactDetail>();

        for (int i = 0; i < CONTACT_DETAIL_PROP_INDEXES.length; i++)
        {
            int property = CONTACT_DETAIL_PROP_INDEXES[i];
            Object value = values[property];

            if (value instanceof String)
            {
                String stringValue = (String) value;

                if (stringValue.length() != 0)
                {
                    if(isPhoneNumber(property))
                        stringValue
                            = PhoneNumberI18nService.normalize(stringValue);

                    MsOutlookAddrBookContactDetail contactDetail
                        = new MsOutlookAddrBookContactDetail(
                                stringValue,
                                getCategory(property),
                                getSubCategories(property),
                                MAPI_MAILUSER_PROP_IDS[property]);

                    // Check if this contact detail support the telephony and
                    // the persistent presence operation set.
                    for(int j = 0;
                            j < CONTACT_OPERATION_SET_ABLE_PROP_INDEXES.length;
                            ++j)
                    {
                        if(property
                                == CONTACT_OPERATION_SET_ABLE_PROP_INDEXES[j])
                        {
                            contactDetail.setSupportedOpSets(supportedOpSets);
                            // Found, then break the loop.
                            j = CONTACT_OPERATION_SET_ABLE_PROP_INDEXES.length;
                        }
                    }
                    contactDetails.add(contactDetail);
                }
            }
        }

        return contactDetails;
    }

    /**
     * Performs this <tt>AsyncContactQuery</tt> in a background <tt>Thread</tt>.
     *
     * @see AsyncContactQuery#run()
     */
    @Override
    protected void run()
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("run query: " + query.toString());
        }
        synchronized (MsOutlookAddrBookContactQuery.class)
        {
            foreachMailUser(
                query.toString(),
                new PtrOutlookContactCallback());
        }
    }

    /**
     * Callback method when receiving notifications for inserted items.
     *
     * @param id The outlook contact identifier.
     */
    public void inserted(String id)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("inserted: " + id);
        }
        SourceContact sourceContact = findSourceContactByID(id);
        if(sourceContact != null
                && sourceContact instanceof MsOutlookAddrBookSourceContact)
        {
            updated(id);
        }
        else
        {
            try
            {
                onMailUser(id);
            }
            catch (MsOutlookMAPIHResultException e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            MsOutlookAddrBookContactQuery.class.getSimpleName()
                            + "#onMailUser(String)",
                            e);
                }
            }
        }
    }

    /**
     * Callback method when receiving notifications for updated items.
     *
     * @param id The outlook contact identifier.
     */
    public void updated(String id)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("updated: " + id);
        }
        SourceContact sourceContact = findSourceContactByID(id);
        if(sourceContact != null
                && sourceContact instanceof MsOutlookAddrBookSourceContact)
        {
            ((MsOutlookAddrBookSourceContact) sourceContact).updated();
            fireContactChanged(sourceContact);
        }
    }

    /**
     * Callback method when receiving notifications for deleted items.
     *
     * @param id The outlook contact identifier.
     */
    public void deleted(String id)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("deleted: " + id);
        }
        if(id != null)
        {
            SourceContact sourceContact = findSourceContactByID(id);

            if(sourceContact != null)
            {
                fireContactRemoved(sourceContact);
            }
        }
    }

    /**
     * Callback to called by the native outlook part with a contact id as
     * argument.
     */
    public class PtrOutlookContactCallback
    {
        /**
         * Notifies this callback about a specific contact.
         *
         * @param id The outlook contact identifier.
         *
         * @return <tt>true</tt> if this <tt>PtrCallback</tt> is to continue
         * being called; otherwise, <tt>false</tt>
         */
        boolean callback(String id)
        {
            try
            {
                return onMailUser(id);
            }
            catch (MsOutlookMAPIHResultException e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            MsOutlookAddrBookContactQuery.class.getSimpleName()
                            + "#onMailUser(String)",
                            e);
                }
                return false;
            }
        }
    }

    /**
     * Adds a new empty contact, which will be filled in later.
     *
     * @param id The ID of the contact to add.
     */
    public void addEmptyContact(String id)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("Add empty contact: " + id);
        }
        if(id != null)
        {
            final MsOutlookAddrBookSourceContact sourceContact
                = new MsOutlookAddrBookSourceContact(
                        getContactSource(),
                        id,
                        null,
                        null,
                        new LinkedList<ContactDetail>());
            addQueryResult(sourceContact);
        }
    }

    /**
     * Gets the <tt>displayName</tt> to be set on a <tt>SourceContact</tt>.
     *
     * @param values the values of the contact properties.
     *
     * @return the <tt>displayName</tt> to be set on a <tt>SourceContact</tt>.
     */
    public static String getDisplayName(Object[] values)
    {
        String displayName = (String) values[PR_NICKNAME];

        if ((displayName == null) || (displayName.length() == 0))
        {
            String firstName = (String) values[PR_GIVEN_NAME];
            String lastName = (String) values[PR_SURNAME];
            if ((lastName == null) || (lastName.length() == 0))
                lastName = (String) values[PR_MIDDLE_NAME];

            if ((firstName == null) || (firstName.length() == 0))
                displayName = lastName;
            else
            {
                displayName = firstName;
                if ((lastName != null) && (lastName.length() != 0))
                    displayName += " " + lastName;
            }
        }

        if ((displayName == null) || (displayName.length() == 0))
            displayName = (String) values[PR_COMPANY_NAME];

        if ((displayName == null) || (displayName.length() == 0))
        {
            for(int i = 0; i < values.length; ++i)
            {
                if(values[i] instanceof String)
                {
                    displayName = (String) values[i];
                    if ((displayName != null) && (displayName.length() != 0))
                        return displayName;
                }
            }
        }

        return displayName;
    }

    /**
     * Gets the organization name to be set on a <tt>SourceContact</tt>.
     *
     * @param values the values of the contact properties.
     *
     * @return the organization name to be set on a <tt>SourceContact</tt>.
     */
    public static String getOrganization(Object[] values)
    {
        return (String) values[PR_COMPANY_NAME];
    }
}