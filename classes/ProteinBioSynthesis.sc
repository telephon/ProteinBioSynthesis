
// SC 3 version 0.3, August 2005

// (first version 0.2, July 2001)
// julian rohrhuber

// distributed under the terms of the GNU General Public License
// full notice in ProteinBioSynthesis main folder
// thanks to sophia rohrhuber and andreas bartenstein


ProteinBioSynthesis : Pattern  {

	classvar <>tripletDict, <>transcriptionDict;
	classvar <>aminoacids, <>startcodon= -1, <>stopcodon=0;

	var <>genome, <>verbose = false;

	*new { arg genome; ^super.newCopyArgs(genome) }


	asStream { ^this.subclassResponsibility }

	translate { arg triplet; // triplet to aminoacid id
		^transcriptionDict.at(tripletDict.at(triplet.asSymbol) )
	}

	translateBack { arg aminoacidIndex; // aminoacid id to triplet
		var index;
		index = transcriptionDict.findKeyForValue(aminoacidIndex);
		^tripletDict.findKeyForValue(index);
	}

	size { ^genome.size }

	transcribe { arg string;
		var codons, size;
		codons = List.new;
		size = string.size;

		(size div: 3).do({ arg i;
			var triplet, value, j;
			i = i * 3;
			j = (i+2).clip(i, size);
			triplet = string.copyRange(i, j);
			value = this.translate(triplet);
			if(verbose) { postf("% ... %   : %", triplet, value, aminoacids.at(value)) };

			if(value.notNil, { codons.add(value) })
		});
		^codons;
	}

	loadData { arg path, finishFunc;
		var file, mode = \text, triplet, value, bases;

		file = File(path, "r");
		bases = this.class.bases;

		protect {

			triplet = String.new(3);
			genome = String.new;


			file.length.do({ arg i;

				var char, trip, codon;
				char = file.getChar;

				//leave out all text that is not relevant
				if(mode == \text, {
					//check if letter is a nuclein acid
					if(bases.includes(char), {
						// check if the letter is followed by a triplet
						trip = file.nextN(3).asSymbol;
						file.pos = file.pos - 3;
						if(tripletDict.at(trip).notNil, { mode = \data });
					})
				}, {
					if(bases.includes(char).not, { mode = \text});
				});

				// fill the codon list with the aminoacid ids
				if(mode == \data, {

					genome = genome ++ char

				}, {
					if(verbose) { char.post };
					//comment if you don't want to see the text that is left out
				})
			});
		} {
			file.close;
		};

		if(verbose) { path.post; ": file is closed again".postln };
		finishFunc.value;


	}

	//the nuclein acids
	*bases {
		^"agtc"
	}

	*initClass {
		var basic, count;

		//mapping of triplets to tripletIDs
		basic = this.bases.collectAs({ arg char; char.asString }, Array);
		tripletDict = IdentityDictionary.new;
		count = 0;
		basic.do({ arg item1;
			basic.do({ arg item2;
				basic.do({ arg item3;
					tripletDict.put((item1++item2++item3).asSymbol, count);
					count = count+1;
				});
			});
		});

		//mapping of the 64 tripletIDs to the 21 aminoacids
		//the genetic code is called 'degenerated', because it is partly redundant.
		//this is the standard translation table for most higher forms of life,
		//the interpretation varies in different organisms.

		transcriptionDict = IdentityDictionary[
			0 -> 1,
			1 -> 1,
			2 -> 2,
			3 -> 2,
			4 -> 3,
			5 -> 3,
			6 -> 3,
			7 -> 3,
			8 -> 4,
			9 -> 4,
			10 -> -1,
			11 -> -1,
			12 -> 5,
			13 -> 5,
			14 -> -1,
			15 -> 6,
			16 -> 2,
			17 -> 2,
			18 -> 2,
			19 -> 2,
			20 -> 7,
			21 -> 7,
			22 -> 7,
			23 -> 7,
			24 -> 8,
			25 -> 8,
			26 -> 9,
			27 -> 9,
			28 -> 10,
			29 -> 10,
			30 -> 10,
			31 -> 10,
			32 -> 11,
			33 -> 11,
			34 -> 11,
			35 -> 0,
			36 -> 12,
			37 -> 12,
			38 -> 12,
			39 -> 12,
			40 -> 13,
			41 -> 13,
			42 -> 14,
			43 -> 14,
			44 -> 2,
			45 -> 2,
			46 -> 10,
			47 -> 10,
			48 -> 15,
			49 -> 15,
			50 -> 15,
			51 -> 15,
			52 -> 16,
			53 -> 16,
			54 -> 16,
			55 -> 16,
			56 -> 17,
			57 -> 17,
			58 -> 18,
			59 -> 18,
			60 -> 19,
			61 -> 19,
			62 -> 19,
			63 -> 19
		];

		//mapping of aminoacids to their names
		aminoacids = IdentityDictionary[
			-1 ->		\stop,		// -> stopcodon
			0 ->		\methionin, 	// -> startcodon
			1 ->		\phenylalanin,
			2 -> 		\leucin,
			3 -> 		\serin,
			4 -> 		\tyrosin,
			5 -> 		\cystein,
			6 -> 		\tryptophan,
			7 -> 		\prolin,
			8 -> 		\histidin,
			9 ->		\glutamin,

			10 ->		\arginin,
			11 ->		\isoleucin,
			12 ->		\threonin,
			13 ->		\asparagin,
			14 ->		\lysin,
			15 ->		\valin,
			16 ->		\alanin,
			17 ->		\asparaginacid,
			18 ->		\glutaminacid,
			19 ->		\glycin
		]
	}


	//another way to do the same.
	loadData2 { arg path, finishFunc;
		var file, mode = \text, triplet, value;
		var string, char, trip, index=0;
		var bases = this.class.bases;

		file = File(path, "rb");
		triplet = String.new(3);
		genome = String.new(30);
		string = file.readAllString;
		file.close;
		if(verbose) { path.post; ": file is closed again".postln };

		string.size.do({ arg i;

			var char, trip, codon;
			char = string.at(index);
			index = index + 1;
			//index.postln;
			//leave out all text that is not relevant
			if(mode == \text, {
				//check if letter is a nuclein acid
				if(bases.includes(char), {
					// check if the letter is followed by a triplet
					trip = string.copyRange(index, index+2).asSymbol;
					if(tripletDict.at(trip).notNil, { mode = \data; });
				})
			}, {
				if(bases.includes(char).not, { mode = \text});
			});

			// fill the codon list with the aminoacid ids
			if(mode == \data, {

				genome = genome ++ char

			}, {

				//char.post //comment if you don't want to see the text that is left out
			});

		});


		finishFunc.value;


	}

}