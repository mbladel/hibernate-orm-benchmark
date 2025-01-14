package org.hibernate.benchmark.enhancement;

import java.net.URL;
import java.sql.Clob;
import java.sql.Types;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityResult;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.JdbcTypeCode;

import org.hibernate.testing.orm.domain.gambit.MutableValue;

/**
 * @author Steve Ebersole
 */
@SuppressWarnings( "unused" )
@SqlResultSetMapping(
		name = "entity-of-basics-implicit",
		entities = @EntityResult( entityClass = EntityOfBasics.class )
)
@Entity
public class EntityOfBasics {

	public enum Gender {
		MALE,
		FEMALE,
		OTHER
	}

	private Integer id;
	private Boolean theBoolean = false;
	private Boolean theNumericBoolean = false;
	private Boolean theStringBoolean = false;
	private String theString;
	private Integer theInteger;
	private int theInt;
	private short theShort;
	private double theDouble;
	private URL theUrl;
	private Clob theClob;
	private Date theDate;
	private Date theTime;
	private Date theTimestamp;
	private Instant theInstant;
	private EntityOfBasics.Gender gender;
	private EntityOfBasics.Gender singleCharGender;
	private EntityOfBasics.Gender convertedGender;
	private EntityOfBasics.Gender ordinalGender;
	private Duration theDuration;
	private UUID theUuid;

	private LocalDateTime theLocalDateTime;
	private LocalDate theLocalDate;
	private LocalTime theLocalTime;
	private ZonedDateTime theZonedDateTime;
	private OffsetDateTime theOffsetDateTime;

	private MutableValue mutableValue;

	private String theField = "the string";

	private String field1;
	private String field2;
	private String field3;
	private String field4;
	private String field5;
	private String field6;
	private String field7;
	private String field8;
	private String field9;
	private String field0;
	private String field11;
	private String field12;
	private String field13;
	private String field14;
	private String field15;
	private String field16;
	private String field17;
	private String field18;
	private String field19;
	private String field10;
	private String field21;
	private String field22;
	private String field23;
	private String field24;
	private String field25;
	private String field26;
	private String field27;
	private String field28;
	private String field29;
	private String field20;
	private String field31;
	private String field32;
	private String field33;
	private String field34;
	private String field35;
	private String field36;
	private String field37;
	private String field38;
	private String field39;
	private String field30;
	private String field41;
	private String field42;
	private String field43;
	private String field44;
	private String field45;
	private String field46;
	private String field47;
	private String field48;
	private String field49;
	private String field40;
	private String field51;
	private String field52;
	private String field53;
	private String field54;
	private String field55;
	private String field56;
	private String field57;
	private String field58;
	private String field59;
	private String field50;

	public String getField1() {
		return field1;
	}

	public void setField1(String field1) {
		this.field1 = field1;
	}

	public String getField2() {
		return field2;
	}

	public void setField2(String field2) {
		this.field2 = field2;
	}

	public String getField3() {
		return field3;
	}

	public void setField3(String field3) {
		this.field3 = field3;
	}

	public String getField4() {
		return field4;
	}

	public void setField4(String field4) {
		this.field4 = field4;
	}

	public String getField5() {
		return field5;
	}

	public void setField5(String field5) {
		this.field5 = field5;
	}

	public String getField6() {
		return field6;
	}

	public void setField6(String field6) {
		this.field6 = field6;
	}

	public String getField7() {
		return field7;
	}

	public void setField7(String field7) {
		this.field7 = field7;
	}

	public String getField8() {
		return field8;
	}

	public void setField8(String field8) {
		this.field8 = field8;
	}

	public String getField9() {
		return field9;
	}

	public void setField9(String field9) {
		this.field9 = field9;
	}

	public String getField0() {
		return field0;
	}

	public void setField0(String field0) {
		this.field0 = field0;
	}

	public String getField11() {
		return field11;
	}

	public void setField11(String field11) {
		this.field11 = field11;
	}

	public String getField12() {
		return field12;
	}

	public void setField12(String field12) {
		this.field12 = field12;
	}

	public String getField13() {
		return field13;
	}

	public void setField13(String field13) {
		this.field13 = field13;
	}

	public String getField14() {
		return field14;
	}

	public void setField14(String field14) {
		this.field14 = field14;
	}

	public String getField15() {
		return field15;
	}

	public void setField15(String field15) {
		this.field15 = field15;
	}

	public String getField16() {
		return field16;
	}

	public void setField16(String field16) {
		this.field16 = field16;
	}

	public String getField17() {
		return field17;
	}

	public void setField17(String field17) {
		this.field17 = field17;
	}

	public String getField18() {
		return field18;
	}

	public void setField18(String field18) {
		this.field18 = field18;
	}

	public String getField19() {
		return field19;
	}

	public void setField19(String field19) {
		this.field19 = field19;
	}

	public String getField10() {
		return field10;
	}

	public void setField10(String field10) {
		this.field10 = field10;
	}

	public String getField21() {
		return field21;
	}

	public void setField21(String field21) {
		this.field21 = field21;
	}

	public String getField22() {
		return field22;
	}

	public void setField22(String field22) {
		this.field22 = field22;
	}

	public String getField23() {
		return field23;
	}

	public void setField23(String field23) {
		this.field23 = field23;
	}

	public String getField24() {
		return field24;
	}

	public void setField24(String field24) {
		this.field24 = field24;
	}

	public String getField25() {
		return field25;
	}

	public void setField25(String field25) {
		this.field25 = field25;
	}

	public String getField26() {
		return field26;
	}

	public void setField26(String field26) {
		this.field26 = field26;
	}

	public String getField27() {
		return field27;
	}

	public void setField27(String field27) {
		this.field27 = field27;
	}

	public String getField28() {
		return field28;
	}

	public void setField28(String field28) {
		this.field28 = field28;
	}

	public String getField29() {
		return field29;
	}

	public void setField29(String field29) {
		this.field29 = field29;
	}

	public String getField20() {
		return field20;
	}

	public void setField20(String field20) {
		this.field20 = field20;
	}

	public String getField31() {
		return field31;
	}

	public void setField31(String field31) {
		this.field31 = field31;
	}

	public String getField32() {
		return field32;
	}

	public void setField32(String field32) {
		this.field32 = field32;
	}

	public String getField33() {
		return field33;
	}

	public void setField33(String field33) {
		this.field33 = field33;
	}

	public String getField34() {
		return field34;
	}

	public void setField34(String field34) {
		this.field34 = field34;
	}

	public String getField35() {
		return field35;
	}

	public void setField35(String field35) {
		this.field35 = field35;
	}

	public String getField36() {
		return field36;
	}

	public void setField36(String field36) {
		this.field36 = field36;
	}

	public String getField37() {
		return field37;
	}

	public void setField37(String field37) {
		this.field37 = field37;
	}

	public String getField38() {
		return field38;
	}

	public void setField38(String field38) {
		this.field38 = field38;
	}

	public String getField39() {
		return field39;
	}

	public void setField39(String field39) {
		this.field39 = field39;
	}

	public String getField30() {
		return field30;
	}

	public void setField30(String field30) {
		this.field30 = field30;
	}

	public String getField41() {
		return field41;
	}

	public void setField41(String field41) {
		this.field41 = field41;
	}

	public String getField42() {
		return field42;
	}

	public void setField42(String field42) {
		this.field42 = field42;
	}

	public String getField43() {
		return field43;
	}

	public void setField43(String field43) {
		this.field43 = field43;
	}

	public String getField44() {
		return field44;
	}

	public void setField44(String field44) {
		this.field44 = field44;
	}

	public String getField45() {
		return field45;
	}

	public void setField45(String field45) {
		this.field45 = field45;
	}

	public String getField46() {
		return field46;
	}

	public void setField46(String field46) {
		this.field46 = field46;
	}

	public String getField47() {
		return field47;
	}

	public void setField47(String field47) {
		this.field47 = field47;
	}

	public String getField48() {
		return field48;
	}

	public void setField48(String field48) {
		this.field48 = field48;
	}

	public String getField49() {
		return field49;
	}

	public void setField49(String field49) {
		this.field49 = field49;
	}

	public String getField40() {
		return field40;
	}

	public void setField40(String field40) {
		this.field40 = field40;
	}

	public String getField51() {
		return field51;
	}

	public void setField51(String field51) {
		this.field51 = field51;
	}

	public String getField52() {
		return field52;
	}

	public void setField52(String field52) {
		this.field52 = field52;
	}

	public String getField53() {
		return field53;
	}

	public void setField53(String field53) {
		this.field53 = field53;
	}

	public String getField54() {
		return field54;
	}

	public void setField54(String field54) {
		this.field54 = field54;
	}

	public String getField55() {
		return field55;
	}

	public void setField55(String field55) {
		this.field55 = field55;
	}

	public String getField56() {
		return field56;
	}

	public void setField56(String field56) {
		this.field56 = field56;
	}

	public String getField57() {
		return field57;
	}

	public void setField57(String field57) {
		this.field57 = field57;
	}

	public String getField58() {
		return field58;
	}

	public void setField58(String field58) {
		this.field58 = field58;
	}

	public String getField59() {
		return field59;
	}

	public void setField59(String field59) {
		this.field59 = field59;
	}

	public String getField50() {
		return field50;
	}

	public void setField50(String field50) {
		this.field50 = field50;
	}

	public EntityOfBasics() {
	}

	public EntityOfBasics(Integer id) {
		this.id = id;
	}

	@Id
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "the_string")
	public String getTheString() {
		return theString;
	}

	public void setTheString(String theString) {
		this.theString = theString;
	}

	@Column(name = "the_integer")
	public Integer getTheInteger() {
		return theInteger;
	}

	public void setTheInteger(Integer theInteger) {
		this.theInteger = theInteger;
	}

	@Column(name = "the_int")
	public int getTheInt() {
		return theInt;
	}

	public void setTheInt(int theInt) {
		this.theInt = theInt;
	}

	@Column(name = "the_short")
	public short getTheShort() {
		return theShort;
	}

	public void setTheShort(short theShort) {
		this.theShort = theShort;
	}

	@Column(name = "the_double")
	public double getTheDouble() {
		return theDouble;
	}

	public void setTheDouble(double theDouble) {
		this.theDouble = theDouble;
	}

	@Column(name = "the_url")
	public URL getTheUrl() {
		return theUrl;
	}

	public void setTheUrl(URL theUrl) {
		this.theUrl = theUrl;
	}

	@Column(name = "the_clob")
	public Clob getTheClob() {
		return theClob;
	}

	public void setTheClob(Clob theClob) {
		this.theClob = theClob;
	}

	@Enumerated( EnumType.STRING )
	public EntityOfBasics.Gender getGender() {
		return gender;
	}

	public void setGender(EntityOfBasics.Gender gender) {
		this.gender = gender;
	}

	@Enumerated( EnumType.STRING )
	@Column( name = "single_char_gender", length = 1 )
	public EntityOfBasics.Gender getSingleCharGender() {
		return singleCharGender;
	}

	public void setSingleCharGender(EntityOfBasics.Gender singleCharGender) {
		this.singleCharGender = singleCharGender;
	}

	@Convert( converter = EntityOfBasics.GenderConverter.class )
	@Column(name = "converted_gender", length = 1)
	@JdbcTypeCode( Types.CHAR )
	public EntityOfBasics.Gender getConvertedGender() {
		return convertedGender;
	}

	public void setConvertedGender(EntityOfBasics.Gender convertedGender) {
		this.convertedGender = convertedGender;
	}

	@Column(name = "ordinal_gender")
	public EntityOfBasics.Gender getOrdinalGender() {
		return ordinalGender;
	}

	public void setOrdinalGender(EntityOfBasics.Gender ordinalGender) {
		this.ordinalGender = ordinalGender;
	}

	@Column(name = "the_date")
	@Temporal( TemporalType.DATE )
	public Date getTheDate() {
		return theDate;
	}

	public void setTheDate(Date theDate) {
		this.theDate = theDate;
	}

	@Column(name = "the_time")
	@Temporal( TemporalType.TIME )
	public Date getTheTime() {
		return theTime;
	}

	public void setTheTime(Date theTime) {
		this.theTime = theTime;
	}

	@Column(name = "the_timestamp")
	@Temporal( TemporalType.TIMESTAMP )
	public Date getTheTimestamp() {
		return theTimestamp;
	}

	public void setTheTimestamp(Date theTimestamp) {
		this.theTimestamp = theTimestamp;
	}

	@Column(name = "the_instant")
	@Temporal( TemporalType.TIMESTAMP )
	public Instant getTheInstant() {
		return theInstant;
	}

	public void setTheInstant(Instant theInstant) {
		this.theInstant = theInstant;
	}

	@Column(name = "the_local_date_time")
	public LocalDateTime getTheLocalDateTime() {
		return theLocalDateTime;
	}

	public void setTheLocalDateTime(LocalDateTime theLocalDateTime) {
		this.theLocalDateTime = theLocalDateTime;
	}

	@Column(name = "the_local_date")
	public LocalDate getTheLocalDate() {
		return theLocalDate;
	}

	public void setTheLocalDate(LocalDate theLocalDate) {
		this.theLocalDate = theLocalDate;
	}

	@Column(name = "the_local_time")
	public LocalTime getTheLocalTime() {
		return theLocalTime;
	}

	public void setTheLocalTime(LocalTime theLocalTime) {
		this.theLocalTime = theLocalTime;
	}

	@Column(name = "the_offset_date_time")
	public OffsetDateTime getTheOffsetDateTime() {
		return theOffsetDateTime;
	}

	public void setTheOffsetDateTime(OffsetDateTime theOffsetDateTime) {
		this.theOffsetDateTime = theOffsetDateTime;
	}

	@Column(name = "the_zoned_date_time")
	public ZonedDateTime getTheZonedDateTime() {
		return theZonedDateTime;
	}

	public void setTheZonedDateTime(ZonedDateTime theZonedDateTime) {
		this.theZonedDateTime = theZonedDateTime;
	}

	@Column(name = "the_duration")
	public Duration getTheDuration() {
		return theDuration;
	}

	public void setTheDuration(Duration theDuration) {
		this.theDuration = theDuration;
	}

	@Column(name = "theuuid")
	public UUID getTheUuid() {
		return theUuid;
	}

	public void setTheUuid(UUID theUuid) {
		this.theUuid = theUuid;
	}

	@Column(name = "the_boolean")
	public Boolean isTheBoolean() {
		return theBoolean;
	}

	public void setTheBoolean(Boolean theBoolean) {
		this.theBoolean = theBoolean;
	}

	@Column(name = "the_numeric_boolean")
	@JdbcTypeCode( Types.INTEGER )
	public Boolean isTheNumericBoolean() {
		return theNumericBoolean;
	}

	public void setTheNumericBoolean(Boolean theNumericBoolean) {
		this.theNumericBoolean = theNumericBoolean;
	}

	@Column(name = "the_string_boolean")
	@JdbcTypeCode( Types.CHAR )
	public Boolean isTheStringBoolean() {
		return theStringBoolean;
	}

	public void setTheStringBoolean(Boolean theStringBoolean) {
		this.theStringBoolean = theStringBoolean;
	}

	@Column(name = "the_column")
	public String getTheField() {
		return theField;
	}

	public void setTheField(String theField) {
		this.theField = theField;
	}

	@Convert( converter = EntityOfBasics.MutableValueConverter.class )
	@Column(name = "mutable_value")
	public MutableValue getMutableValue() {
		return mutableValue;
	}

	public void setMutableValue(MutableValue mutableValue) {
		this.mutableValue = mutableValue;
	}

	public static class MutableValueConverter implements AttributeConverter<MutableValue,String> {
		@Override
		public String convertToDatabaseColumn(MutableValue attribute) {
			return attribute == null ? null : attribute.getState();
		}

		@Override
		public MutableValue convertToEntityAttribute(String dbData) {
			return dbData == null ? null : new MutableValue( dbData );
		}
	}

	public static class GenderConverter implements AttributeConverter<EntityOfBasics.Gender,Character> {
		@Override
		public Character convertToDatabaseColumn(EntityOfBasics.Gender attribute) {
			if ( attribute == null ) {
				return null;
			}

			if ( attribute == EntityOfBasics.Gender.OTHER ) {
				return 'O';
			}

			if ( attribute == EntityOfBasics.Gender.MALE ) {
				return 'M';
			}

			return 'F';
		}

		@Override
		public EntityOfBasics.Gender convertToEntityAttribute(Character dbData) {
			if ( dbData == null ) {
				return null;
			}

			if ( 'O' == dbData ) {
				return EntityOfBasics.Gender.OTHER;
			}

			if ( 'M' == dbData ) {
				return EntityOfBasics.Gender.MALE;
			}

			return EntityOfBasics.Gender.FEMALE;
		}
	}
}

