package net.sf.okapi.steps.gcaligner;

public class Penalties {
	/* -100 * log([prob of 2-1 match] / [prob of 1-1 match]) */
	public final static int PENALTY_21 = 230; // orig 400
	/* -100 * log([prob of 2-2 match] / [prob of 1-1 match]) */
	public final static int PENALTY_22 = 440; // orig 440
	/* -100 * log([prob of 0-1 match] / [prob of 1-1 match]) */
	public final static int PENALTY_01 = 450; // orig 100

	public int penalty2_1 = PENALTY_21;
	public int penalty2_2 = PENALTY_22;
	public int penalty0_1 = PENALTY_01;
}
