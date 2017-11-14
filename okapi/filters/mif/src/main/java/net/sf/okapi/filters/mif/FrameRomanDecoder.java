/*===========================================================================
  Copyright (C) 2011-2012 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
===========================================================================*/

package net.sf.okapi.filters.mif;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * Implements CharsetDecoder for the FrameMaker Roman character set.
 * This goes from the MIF bytes to the Unicode characters.
 */
class FrameRomanDecoder extends CharsetDecoder {

	protected FrameRomanDecoder (Charset charset,
		float averageCharsPerByte,
		float maxCharsPerByte)
	{
		super(charset, averageCharsPerByte, maxCharsPerByte);
	}

	@Override
	protected CoderResult decodeLoop (ByteBuffer in,
		CharBuffer out)
	{
		while ( in.hasRemaining() ) {
			// First, check if we can output
			if ( !out.hasRemaining() ) {
				// If not: return and tell the caller
				return CoderResult.OVERFLOW;
			}
			// Get the Unicode value for the given byte
			// Make sure we use the un-signed value of the byte
			char outChar = byteToChar[0xFF & in.get()];
			// Write the character in the output buffer
			out.put(outChar);
		}
		// Done
		return CoderResult.UNDERFLOW;
	}

	// map FrameRoman byte to Unicode character
	private static final char[] byteToChar = {
	//	Unicode		<--	byte value (decimal and hexa)	
		'\ufffd',	//	0	0
		'\ufffd',	//	1	1
		'\ufffd',	//	2	2
		'\ufffd',	//	3	3
		'\u00ad',	//	4	4
		'\u200d',	//	5	5
		'\u2010',	//	6	6
		'\ufffd',	//	7	7
		'\u0009',	//	8	8
		'\u0009',	//	9	9
		'\n',		//	10	A
		'\ufffd',	//	11	B
		'\ufffd',	//	12	C
		'\r',		//	13	D
		'\ufffd',	//	14	E
		'\ufffd',	//	15	F
		'\u2007',	//	16	10
		'\u00a0',	//	17	11
		'\u2009',	//	18	12
		'\u2002',	//	19	13
		'\u2003',	//	20	14
		'\u2011',	//	21	15
		'\ufffd',	//	22	16
		'\ufffd',	//	23	17
		'\ufffd',	//	24	18
		'\ufffd',	//	25	19
		'\ufffd',	//	26	1A
		'\ufffd',	//	27	1B
		'\ufffd',	//	28	1C
		'\ufffd',	//	29	1D
		'\ufffd',	//	30	1E
		'\ufffd',	//	31	1F
		
		'\u0020',	//	32	20 - Normal ASCII start
		'\u0021',	//	33	21
		'\u0022',	//	34	22
		'\u0023',	//	35	23
		'\u0024',	//	36	24
		'\u0025',	//	37	25
		'\u0026',	//	38	26
		'\'',		//	39	27
		'\u0028',	//	40	28
		'\u0029',	//	41	29
		'\u002a',	//	42	2A
		'\u002b',	//	43	2B
		'\u002c',	//	44	2C
		'\u002d',	//	45	2D
		'\u002e',	//	46	2E
		'\u002f',	//	47	2F
		'\u0030',	//	48	30
		'\u0031',	//	49	31
		'\u0032',	//	50	32
		'\u0033',	//	51	33
		'\u0034',	//	52	34
		'\u0035',	//	53	35
		'\u0036',	//	54	36
		'\u0037',	//	55	37
		'\u0038',	//	56	38
		'\u0039',	//	57	39
		'\u003a',	//	58	3A
		'\u003b',	//	59	3B
		'\u003c',	//	60	3C
		'\u003d',	//	61	3D
		'\u003e',	//	62	3E
		'\u003f',	//	63	3F
		'\u0040',	//	64	40
		'\u0041',	//	65	41
		'\u0042',	//	66	42
		'\u0043',	//	67	43
		'\u0044',	//	68	44
		'\u0045',	//	69	45
		'\u0046',	//	70	46
		'\u0047',	//	71	47
		'\u0048',	//	72	48
		'\u0049',	//	73	49
		'\u004a',	//	74	4A
		'\u004b',	//	75	4B
		'\u004c',	//	76	4C
		'\u004d',	//	77	4D
		'\u004e',	//	78	4E
		'\u004f',	//	79	4F
		'\u0050',	//	80	50
		'\u0051',	//	81	51
		'\u0052',	//	82	52
		'\u0053',	//	83	53
		'\u0054',	//	84	54
		'\u0055',	//	85	55
		'\u0056',	//	86	56
		'\u0057',	//	87	57
		'\u0058',	//	88	58
		'\u0059',	//	89	59
		'\u005a',	//	90	5A
		'\u005b',	//	91	5B
		'\\',		//	92	5C
		'\u005d',	//	93	5D
		'\u005e',	//	94	5E
		'\u005f',	//	95	5F
		'\u0060',	//	96	60
		'\u0061',	//	97	61
		'\u0062',	//	98	62
		'\u0063',	//	99	63
		'\u0064',	//	100	64
		'\u0065',	//	101	65
		'\u0066',	//	102	66
		'\u0067',	//	103	67
		'\u0068',	//	104	68
		'\u0069',	//	105	69
		'\u006a',	//	106	6A
		'\u006b',	//	107	6B
		'\u006c',	//	108	6C
		'\u006d',	//	109	6D
		'\u006e',	//	110	6E
		'\u006f',	//	111	6F
		'\u0070',	//	112	70
		'\u0071',	//	113	71
		'\u0072',	//	114	72
		'\u0073',	//	115	73
		'\u0074',	//	116	74
		'\u0075',	//	117	75
		'\u0076',	//	118	76
		'\u0077',	//	119	77
		'\u0078',	//	120	78
		'\u0079',	//	121	79
		'\u007a',	//	122	7A
		'\u007b',	//	123	7B
		'\u007c',	//	124	7C
		'\u007d',	//	125	7D
		'\u007e',	//	126	7E
		'\u007f',	//	127	7F - Normal ASCII end
		
		'\u00c4',	//	128	80
		'\u00c5',	//	129	81
		'\u00c7',	//	130	82
		'\u00c9',	//	131	83
		'\u00d1',	//	132	84
		'\u00d6',	//	133	85
		'\u00dc',	//	134	86
		'\u00e1',	//	135	87
		'\u00e0',	//	136	88
		'\u00e2',	//	137	89
		'\u00e4',	//	138	8A
		'\u00e3',	//	139	8B
		'\u00e5',	//	140	8C
		'\u00e7',	//	141	8D
		'\u00e9',	//	142	8E
		'\u00e8',	//	143	8F
		'\u00ea',	//	144	90
		'\u00eb',	//	145	91
		'\u00ed',	//	146	92
		'\u00ec',	//	147	93
		'\u00ee',	//	148	94
		'\u00ef',	//	149	95
		'\u00f1',	//	150	96
		'\u00f3',	//	151	97
		'\u00f2',	//	152	98
		'\u00f4',	//	153	99
		'\u00f6',	//	154	9A
		'\u00f5',	//	155	9B
		'\u00fa',	//	156	9C
		'\u00f9',	//	157	9D
		'\u00fb',	//	158	9E
		'\u00fc',	//	159	9F
		'\u2020',	//	160	A0
		'\u00b0',	//	161	A1
		'\u00a2',	//	162	A2
		'\u00a3',	//	163	A3
		'\u00a7',	//	164	A4
		'\u2022',	//	165	A5
		'\u00b6',	//	166	A6
		'\u00df',	//	167	A7
		'\u00ae',	//	168	A8
		'\u00a9',	//	169	A9
		'\u2122',	//	170	AA
		'\u00b4',	//	171	AB
		'\u00a8',	//	172	AC
		'\u00a6',	//	173	AD
		'\u00c6',	//	174	AE
		'\u00d8',	//	175	AF
		'\u00d7',	//	176	B0
		'\u00b1',	//	177	B1
		'\u00f0',	//	178	B2
		'\u0160',	//	179	B3
		'\u00a5',	//	180	B4
		'\u00b5',	//	181	B5
		'\u00b9',	//	182	B6
		'\u00b2',	//	183	B7
		'\u00b3',	//	184	B8
		'\u00bc',	//	185	B9
		'\u00bd',	//	186	BA
		'\u00aa',	//	187	BB
		'\u00ba',	//	188	BC
		'\u00be',	//	189	BD
		'\u00e6',	//	190	BE
		'\u00f8',	//	191	BF
		'\u00bf',	//	192	C0
		'\u00a1',	//	193	C1
		'\u00ac',	//	194	C2
		'\u00d0',	//	195	C3
		'\u0192',	//	196	C4
		'\u00dd',	//	197	C5
		'\u00fd',	//	198	C6
		'\u00ab',	//	199	C7
		'\u00bb',	//	200	C8
		'\u2026',	//	201	C9
		'\u00fe',	//	202	CA
		'\u00c0',	//	203	CB
		'\u00c3',	//	204	CC
		'\u00d5',	//	205	CD
		'\u0152',	//	206	CE
		'\u0153',	//	207	CF
		'\u2013',	//	208	D0
		'\u2014',	//	209	D1
		'\u201c',	//	210	D2
		'\u201d',	//	211	D3
		'\u2018',	//	212	D4
		'\u2019',	//	213	D5
		'\u00f7',	//	214	D6
		'\u00de',	//	215	D7
		'\u00ff',	//	216	D8
		'\u0178',	//	217	D9
		'\u2044',	//	218	DA
		'\u00a4',	//	219	DB
		'\u2039',	//	220	DC
		'\u203a',	//	221	DD
		'\ufb01',	//	222	DE
		'\ufb02',	//	223	DF
		'\u2021',	//	224	E0
		'\u00b7',	//	225	E1
		'\u201a',	//	226	E2
		'\u201e',	//	227	E3
		'\u2030',	//	228	E4
		'\u00c2',	//	229	E5
		'\u00ca',	//	230	E6
		'\u00c1',	//	231	E7
		'\u00cb',	//	232	E8
		'\u00c8',	//	233	E9
		'\u00cd',	//	234	EA
		'\u00ce',	//	235	EB
		'\u00cf',	//	236	EC
		'\u00cc',	//	237	ED
		'\u00d3',	//	238	EE
		'\u00d4',	//	239	EF
		'\u0161',	//	240	F0
		'\u00d2',	//	241	F1
		'\u00da',	//	242	F2
		'\u00db',	//	243	F3
		'\u00d9',	//	244	F4
		'\u20ac',	//	245	F5
		'\u02c6',	//	246	F6
		'\u02dc',	//	247	F7
		'\u00af',	//	248	F8
		'\u02c7',	//	249	F9
		'\u017d',	//	250	FA
		'\u02da',	//	251	FB
		'\u00b8',	//	252	FC
		'\u02dd',	//	253	FD
		'\u017e',	//	254	FE
		'\ufffd'	//	255	FF
	};
	
}
