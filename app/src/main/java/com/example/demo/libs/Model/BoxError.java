package com.example.demo.libs.Model;

public final class BoxError
{
	private static final String LOG_TAG = "SLSDK";
	private static final String LOG_CLASS = "[BoxError]";

	public static void ThrowError(BoxErrorCode boxErrorCode, String str) throws BoxException
	{
		BleLog.e(LOG_TAG, LOG_CLASS + "[ThrowError] boxErrorCode=" + boxErrorCode + " str=" + str);

		String errorCode;
		String message;

		/*Set Error message*/
		switch (boxErrorCode)
		{
			case SDK_000:

				errorCode = "SDK-000";
				message = "Unknown " + str;

				break;

			case SDK_001 :

				errorCode = "SDK-001";
				message = "Bluetooth is not be activated";

				break;

			case SDK_002 :

				errorCode = "SDK-002";
				message = "Connection impossible, the boxId " + str + " does not respond";

				break;

			case SDK_003 :

				errorCode = "SDK-003";
				message = "Invalid parameter" + str;

				break;

			case SDK_004:

				errorCode = "SDK-004";
				message = "For COLCFM reason, the parcelsmallHash is Mandatory";

				break;

			case BOX_001 :

				errorCode = "BOX-001";
				message = "The token used is expired, please renew it";

				break;

			case BOX_002 :

				errorCode = "BOX-002";
				message = "The token value cannot be deciphered, please renew it";

				break;

			case BOX_003 :

				errorCode = "BOX-003";
				message = "Invalid parameter " + str;

				break;

			case BOX_004 :

				errorCode = "BOX-004";
				message = "The requested information has not been setup yet or it is not available";

				break;

			case BOX_005 :

				errorCode = "BOX-005";
				message = "The token value cannot be null as the AES key is already set";

				break;

			case BOX_006 :

				errorCode = "BOX-006";
				message = "The requested information have not been setup yet";

				break;

			case BOX_007 :

				errorCode = "BOX-007";
				message = "Hash code does not match with firmware received";

				break;

			case BOX_008 :

				errorCode = "BOX-008";
				message = "Battery level unavailable or cannot be calculated";

				break;
/*
			case BOX_009 :

				errorCode = "BOX-009";
				message = "AES key already exists";

				break;
*/
			case BOX_010 :

				errorCode = "BOX-010";
				message = "Fail to generate the AES key";

				break;
/*
			case BOX_011 :

				errorCode = "BOX-011";
				message = "COLCFM : Already Collected";

				break;

			case BOX_012 :

				errorCode = "BOX-012";
				message = "ENXCFP : Unloaded Expired";

				break;

			case BOX_013 :

				errorCode = "BOX-013";
				message = "ENDBLK : Unloaded blocked (parcel rerouted)";

				break;

			case BOX_014 :

				errorCode = "BOX-014";
				message = "LIVBLK : Unloaded blocked (pick up blocked)";

				break;

			case BOX_015 :

				errorCode = "BOX-015";
				message = "ENDCFP : Unloaded Toolbox";

				break;

			case BOX_016 :

				errorCode = "BOX-016";
				message = "SOLLOS : Parcel lost";

				break;
*/
			case BOX_017 :

				errorCode = "BOX-017";
				message = "MAXATT : Reattempt max";

				break;
/*
			case BOX_018 :

				errorCode = "BOX-018";
				message = "USEDAT : Pickup limitation time";

				break;

			case BOX_019 :

				errorCode = "BOX-019";
				message = "The Box is full, impossible to Load the parcel";

				break;

			case BOX_020 :

				errorCode = "BOX-020";
				message = "The Box is disabled, the Box cannot be opened";

				break;

			case BOX_021 :

				errorCode = "BOX-021";
				message = "USEATT : Pickup limitation attempt";

				break;

			case BOX_022 :

				errorCode = "BOX-022";
				message = "Not enough memory";

				break;
*/
			case BOX_023 :

				errorCode = "BOX-023";
				message = "Battery level is low, cannot perform the update";

				break;
/*
			case BOX_024 :

				errorCode = "BOX-024";
				message = "WROPAR : Wrong parcel number";

				break;
*/
			case BOX_025 :

				errorCode = "BOX-025";
				message = "The privileges do not allow that action be proceeded";

				break;

			case BOX_026:

				errorCode = "BOX-026";
				message = "The token is blacklisted, please renew it";

				break;

			case BOX_027:

				errorCode = "BOX-027";
				message = "Parcel already contains a parcel";

				break;

			default :

				errorCode = "XXX-000";
				message = "Unknown error code";

				break;
		}

		BleLog.e(LOG_TAG, LOG_CLASS + "[ThrowError] throw errorCode=" + errorCode + " message=" + message);

		throw new BoxException(errorCode + " :" + message);
	}
}