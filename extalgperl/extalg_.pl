#!/usr/bin/perl
#
# $Header: X:/CVS/extalg/extalgperl/extalg_.pl,v 1.1.1.1 2000/11/06 21:54:49 pbrown Exp $
#
# $Log: extalg_.pl,v $
# Revision 1.1.1.1  2000/11/06 21:54:49  pbrown
# extalg import
#
# Revision 1.3  1999/04/17 09:52:52  pbrown
# Fixed next_on_path (again)
#
# Revision 1.1  1998/05/27 04:04:53  pbrown
# Initial revision
#
#
#

##############################################################################
# rev: returns reversed string module, i.e., reverses direction and
# changes sense of the arrows; e.g. &rev("a<b<c>d") returns "d<c>b>a";
# both $a and &rev($a) represent the same string module
#
sub rev {
  local ($a)=join('',reverse(split(//,@_[0])));
  $a=~tr/<>/></;
  return $a;
}

##############################################################################
# init_projectives: constructs associative array %proj where $proj{"s") is
# projective cover of simple module represented by "s"
#
sub init_projectives {
  local ($top);
  local ($rev);
  while (<>)
  {
    $rev=0;
    chop;
    if (/^(.)$/)
      {$top=$1;}
    elsif (/<(.)>/)
      {$top=$1;}
    elsif (/^(.)>/)
      {$top=$1;}
    elsif (/<(.)$/)
      {$top=$1; $rev=1;}
    else {die "Illegal format for ",$_ ;}
    $proj{$top} = $rev ? &rev($_) : $_;
    $is_projective{$_}=$is_projective{&rev($_)}=1;
  }
}

##############################################################################
# init_injectives: constructs associative array %inj where $inj{"s") is
# injective envelope of simple module represented by "s"...doesn't include
# NUPI's
# ..uses %lmaxup, %rmaxup, so call &getmaxup first
#
sub init_injectives {
  local (%i, $u);
  foreach $k (keys %lmaxup) {
    $i{$k}=$lmaxup{$k};
  }
  foreach $k (keys %rmaxup) {
    if ($i{$k}) {
      $i{$k}=&splice(&rev($rmaxup{$k}), $inj{$k});
    }
    else {
      $i{$k}=$rmaxup{$k};
    }
  }
  foreach $k (keys %i) {
    unless ($is_dradnupi{$i{$k}}) {
      $u=$inj{$k}=$i{$k};
      $is_injective{$u}=$is_injective{&rev($u)}=1;
    }
  }
}

##############################################################################
# samestring: &samestring($a,$b) is TRUE if $a, $b represent same string
# module, i.e., either $a eq $b, or $a eq &rev($b) 
#
sub samestring {
  return (@_[0] eq @_[1]) || (@_[0] eq &rev(@_[1]));
}

##############################################################################
# splice: splices two stringmods along common vertex...
# e.g., &splice("...y>x", "x<z...")=...y>x<z...returns "" if 
# 'common' vertices are different, or if y==z.
#
sub splice {
  local ($len0)=length(@_[0]);
  local ($len1)=length(@_[1]);
  local ($err)="Attempted to splice misformed strings @_[0] and @_[1]";
  local ($x);
  local ($y);
  if ($len0==0 || $len1==0) {
    return "";
  }
  elsif (substr(@_[0],$len0-1,1) ne substr(@_[1],0,1)) {
    return "";
  }
  elsif ($len0==1) {
    return @_[1];
  }
  elsif ($len1==1) {
    return @_[0];
  }
  elsif ($len0==2 || $len1==2) {
    die $err;
  }
  elsif (substr(@_[0],$len0-3,1) eq substr(@_[1],2,1)) {
    return "";
  }
  else {
    return @_[0].substr(@_[1],1);
  }
}

##############################################################################
# init_radnupi: creates associative arrays 
#             %radnupi: list of radicals of nonuniserial projective/injective modules
#             %dradnupi: list of duals of radicals of nonunis proj/inj (modulo socle)
#
sub init_radnupi {
  local ($k, $u);
  local (@a);

  foreach $k (keys %proj) 
  {
    if ($proj{$k}=~/^((.)<.*)<.>(.*>(.))$/ && $2 eq $4)
    { 
      $u=$nupi{$k}=$proj{$k};
      $is_nupi{$u}=$is_nupi{&rev($u)}=1;
      $u=$radnupi{$k}=&splice($3,$1);
      $is_radnupi{$u}=$is_radnupi{&rev($u)}=1;
      $u=$dradnupi{$k}=substr($proj{$k},2,-2);
      $is_dradnupi{$u}=$is_dradnupi{&rev($u)}=1;
    }
  }
}

##############################################################################
# dradnupi: &dradnupi($a) returns FALSE (empty string) if $a is not 
# non-uniserial projective/injective. If $a is NUPI, returns 
# representation of $a/soc $a (dual of radical) as string module 
#
sub get_dradnupi {
  local ($a)=@_[0];
  if ($a=~/^(.)<(.*<.>.*)>(.)$/ && $1 eq $3)
    {return $2;}
  else
    {return "";}
}

##############################################################################
# top_dradnupi: if $a is a NUPI modulo socle, returns the top, otherwise
# returns empty
#
sub top_dradnupi {
  if (@_[0]=~/<(.)>/) {
    return $1 if ($dradnupi{$1});
  }
  return "";
}

##############################################################################
# init_maxdown: creates associative arrays %lmaxdown, %rmaxdown, where  
# for each vertex 's', $lmaxdown('s') and %rmaxdown('s') are 
# paths starting at 's' maximal wrt property that they are not part
# of a relation defining the algebra; one of these paths may be 
# empty, and one may be trivial (i.e., only 's' itself)
#
sub init_maxdown {
  local ($k);
  local (@a);
  local ($p);
  local ($n);
  foreach $k (keys %proj) 
  {
    if ($n=&get_dradnupi($proj{$k}))
      {$p=$n;}
    else
      {$p=$proj{$k};}
    if ($p=~/<.>/)
    {
      @a=split($k,$p);
      $lmaxdown{$k}=&rev($a[0].$k) if ($a[0]);
      $rmaxdown{$k}=$k.$a[1] if ($a[1]);
      if (!$lmaxdown{$k} && !$rmaxdown{$k})
        {$lmaxdown{$k}=$k;}
    }
    else
      {$lmaxdown{$k}=$p;}
  }
}

##############################################################################
# init_maxup: similar to init_maxdown, except creates associative arrays
# %lmaxup, %rmaxup with paths ending at given vertex.
#
sub init_maxup {
  local ($k, @p, $p, $s);
  foreach $k (keys %proj) 
  {
    push(@p,(&rev($lmaxdown{$k}))) if ($lmaxdown{$k});
    push(@p,(&rev($rmaxdown{$k}))) if ($rmaxdown{$k});
    while (@p)
    {
      $p=pop @p;
      while ($p)
      {
        $s=substr($p,0,1);
        if ($lmaxup{$s})
        {
          if ($p=~/^$lmaxup{$s}/)
            {$lmaxup{$s}=$p;}
          elsif ($lmaxup{$s}=~/^$p/){}
          else
          {
            if ($rmaxup{$s})
              {$rmaxup{$s}=$p if ($p=~/^$rmaxup{$s}/);}
            else
              {$rmaxup{$s}=$p;}
          }
        } 
        else
          {$lmaxup{$s}=$p;}
        if ($p=~/.<(.*)/)
          {$p=$1;}
        else
          {last;}
      }
    }
  }
}

##############################################################################
# tail: returns 'tail' of stringmod, i.e., maximal terminal substring of form
#       .>.>...>.
#
sub tail {
  if (@_[0]=~/([^<>]>[^<]*$)/)
    {return $1;}
  elsif (@_[0]=~/(.)$/)
    {return $1;}
  else
    {return "";}
}

##############################################################################
# cut_tail: cuts the tail off...does nothing if no tail..returns empty if
#          stringmod is its own tail
#
sub cut_tail {
  local ($a)=@_[0];
  local ($tail)=&tail($a);
  if (!$tail)
    {return $a;}
  elsif ($a=~/(.*)<$tail/)
    {return $1;}
  else
    {return "";}
}

##############################################################################
# extend_tail: extends tail by one vertex if possible...returns result as list...
# list is empty if no extension is possible...otherwise, returns list of 
# possible extensions, which always consists of one element unless the
# module is simple, in which case there may be two extensions.
#
sub extend_tail {
  local (@ext);
  local ($tail)=&tail(@_[0]);
  local ($k)=substr($tail,0,1);
  local ($max);
  foreach $max ($lmaxdown{$k},$rmaxdown{$k})
  {
    if ($max=~/^$tail[>](.)/)
      {push(@ext,@_[0].">".$1) unless @_[0] =~/$1<.$/;}
  }
  return @ext;
}

##############################################################################
# RL: &R($a)=&RL(0,$a) 
#     &L($a)=&RL(1,$a)
#
sub RL {
  local ($L)=@_[0];
  local ($a)=@_[1];
  local (@ext);
  local ($x, $k, $sp, $max);

  $a=&rev($a) if ($L);
  @ext=&extend_tail($a);
  if ($#ext==-1)
    {return &cut_tail($a);}
  $x=$ext[0];
  if ($L && length($a)==1)
  { 
    if ($#ext==1) 
      {$x=$ext[1];} 
    else 
      {return "";}
  }
  $k=substr($x,length($x)-1);
  foreach $max ($lmaxup{$k},$rmaxup{$k})
  {
    $sp=&splice($x,$max);
    return $sp if ($sp);
  }
  if ($L)
    {return &rev($x);}
  else
    {return $x;}
}


##############################################################################
# R:
#
#
sub R {
  local ($v)=$R{@_[0]};
  if ($v) {
    return $v;
  }
  else {
    return $R{@_[0]}=&RL(0, @_[0]);
  }
}

##############################################################################
# L:
#
#
sub L {
  local ($v)=$L{@_[0]};
  if ($v) {
    return $v;
  }
  else {
    return $L{@_[0]}=&RL(1, @_[0]);
  }
}

##############################################################################
# is_simple:
# returns true iff string module is simple
sub is_simple {
  length(@_[0])==1;
}

##############################################################################
# next_on_path:
# given v2->v1, next_on_path returns v3 such that v3->v2->v1 is sectional
# path, if it exists
#
sub next_on_path {
  local ($v2, $v1)=(@_[0], @_[1]);
  local ($v2_L, $v2_R);
  if ($v2_L=&L($v2)) {
    return $v2_L unless &samestring($v2_L, &DTr($v1));
  }
  if ($v2_R=&R($v2)) {
    return $v2_R unless &samestring($v2_R, &DTr($v1));
  }
  return "";
}

##############################################################################
# get_DTr:
# computes the dual of the transpose
#
sub get_DTr {
  local ($v)=@_[0];
  local ($u, $u_R, $v_LL, $v_RL);
  if ($is_projective{$v}) {
    return "";
  }
  elsif (&L($v) && &R($v)) {
    if ($v_LL=&L(&L($v))) {
      return $v_LL if &samestring($v_LL, &L(&R($v)));
      return &L(&L($v)) if &samestring($v_LL, &R(&R($v)));
    }
    if ($v_RL=&R(&L($v))) {
      return $v_RL if &samestring($v_RL, &L(&R($v)));
      return $v_RL if &samestring($v_RL, &R(&R($v)));
    }
  }
  else {
    $u=&L($v) ? &L($v) : &R($v);
    $u_R=&R($u);
    if ($u_R) {
      if ($u=~/^$u_R[<>](.*)/) {
        return &samestring($1,$v) ? $u_R : &L($u);
      }
      elsif (&rev($u)=~/(.*)[<>]$u_R$/) {
        return &samestring($1,$v) ? $u_R : &L($u);
      }
    }
    return &L($u);
  }
  # failed
  die "Failed to evaluate DTr($v)";
}

##############################################################################
# DTr:
# returns the dual of the transpose
#
sub DTr {
  local ($v)=$DTr{@_[0]};
  if ($v) {
    return $v;
  }
  else {
    return $DTr{@_[0]}=&get_DTr(@_[0]);
  }
}

##############################################################################
# show_diagnostics:
#
sub show_diagnostics {
  print "PROJECTIVES:\n";
  foreach $k (keys %proj) 
    {print $k,": ",$proj{$k},"\n";}
  
  print "\n";
  
  print "INJECTIVES:\n";
  foreach $k (keys %inj) 
    {print $k,": ",$inj{$k},"\n";}
  
  print "\n";
  
  print "MAXDOWN:\n";
  foreach $k (keys %proj) 
  {
    print $lmaxdown{$k},"\n" if ($lmaxdown{$k});
    print $rmaxdown{$k},"\n" if ($rmaxdown{$k});
  }
  
  print "MAXUP:\n";
  foreach $k (keys %proj) 
  {
    print $lmaxup{$k},"\n" if ($lmaxup{$k});
    print $rmaxup{$k},"\n" if ($rmaxup{$k});
  }
    
  print "RADNUPI:\n";
  foreach $k (keys %radnupi) 
  {
    print $k," ",$radnupi{$k},"\n";
  }
  
  print "DRADNUPI:\n";
  foreach $k (keys %dradnupi) 
  {
    print $k," ",$dradnupi{$k},"\n";
  }
  
  print "==================================================\n";
  
  print "DTr-orbits of injectives:\n";
  foreach $k (keys %inj)
  {
    $a=$inj{$k};
    while ($a) {
      print "$a  ";
      $a=&DTr($a);
    }
    print "\n\n";
  }
}

##############################################################################
# advance:
# given v1, v2 where v1->v2 is irreducible (e.g., v1=R(v2) or L(v2)),
# returns v0, v1, where v0->v1->v2 is sectional
#
sub advance {
  local ($v1, $v2)=@_;
  local ($v0)=&next_on_path($v1, $v2);
  return ($v0, $v1);
}

##############################################################################
# get_rectangular_chains:
#
sub get_rectangular_chains {
  local ($v, $x, $y)=@_;
  local ($v1, $v2, $w1, $w2, $vpeak, $max, $path, $dtrv2);

  if ($is_projective{$v}) { # or if chain is periodic
    return;
  }
  $v2=$w2=$v;
  $v1=&R($v);
  $w1=&L($v);
  $path=0;
  $max=-1;

  for (;;) {
    while ($w2 && !$is_projective{$w2}) {
      print "$v1 ";
      $dtrv2=&DTr($v2);
      if (!$v1 || $is_projective{$v1} || ($max!=-1 && $path >= $max)) {
        last;
      }
      else {
        $path++;
        ($v1, $v2)=&advance($v1, $v2);
      }
    }
    print "\n";
    if (($is_projective{$v1} && $vpeak) || !$w2 || $is_projective{$w2}) {
      print "get_rectangular_chains($vpeak)\n";
      get_rectangular_chains($vpeak);
    }
    if (!$w2 || $is_projective{$w2}) {
      last;
    }
    else {
      $max=$path;
      $path=0;
      $vpeak=$dtrv2;
      $v1=&DTr($w2);
      ($w1, $w2)=&advance($w1, $w2);
      $v2=$w2;
    }
  }
}

##############################################################################
# get_cone:
#
# gets 'cone' of module $v, i.e., all modules on sectional paths beginning
# at module in the DTr-orbit of $v...the cone is returned as a hash of 
# hashes indexed as follows:
# (1) $v=$c{0}{0}
#
# (2) meshes are in rectangular coordinates, i.e.,
#  
#                  $c{i+1}{j+1}
#                /           \
#      $c{i+2}{j}              $c{i}{j}
#                \           /
#                  $c{i+1}{j-1}
#  
#
sub get_cone {
  local ($v)=@_[0];
  local ($v2, $w2, $v1_, $w1_, $v2_, $w2_, $s);
  local (%DTrv);
  local ($i, $i_, $j_);
  local (%c);

  $i=0;
  $v2=&R($v);
  $w2=&L($v);
  while ($v && ! $DTrv{$v}) {
    # keep track of DTr-orbit of $v in case AR-quiver is periodic
    $DTrv{$v}=1;
    $DTrv{&rev($v)}=1;
    $c{$i}{0}=$v;
    $v1_=$w1_=$v;
    $v2_=$v2;
    $w2_=$w2;
    $j_=0;
    $i_=$i;
    while ($v2_) {
			if ($s=&top_dradnupi($v2_)) {
				$c{$i_+1}{$j}=$nupi{$s};
			}
      $c{++$i_}{++$j_}=$v2_;
      ($v2_,$v1_)=&advance($v2_,$v1_);
    }
    $i_=$i;
    $j_=0;
    while ($w2_) {
			if ($s=&top_dradnupi($w2_)) {
				$c{$i_+1}{$j}=$nupi{$s};
			}
      $c{++$i_}{--$j_}=$w2_;
      ($w2_,$w1_)=&advance($w2_,$w1_);
    }
    $v2=&DTr($v2);
    $w2=&DTr($w2);
    $v=&DTr($v);
    $i+=2;
  }
  return %c;
}

##############################################################################
# cone_min_max:
#
# given cone %c, which is hash of hashes, returns
#
# i_min = min { i | $c{i}{j} is defined}
# i_max = max { i | $c{i}{j} is defined}
# j_min = min { j | $c{i}{j} is defined}
# j_max = max { j | $c{i}{j} is defined}
#
# as list (i_min, i_max, j_min, j_max)
#
sub cone_min_max {
  local ($cref)=shift;
  local (@i_)=sort {$a <=> $b} keys %$cref;
  local (@j_, %k, $i, $j);
  foreach $i (keys %$cref) {
    foreach $j (keys %{$$cref{$i}}) {
      $k{$j}=1;
    }
  }
   @j_=sort {$a <=> $b} keys %k;
  return ($i_[0], $i_[-1], $j_[0], $j_[-1]);
}

##############################################################################
# get_basis:
#
sub get_basis {
  local ($k);
  foreach $k (keys %proj) {
    print "get_rectangular_chains($k)\n";
    get_rectangular_chains($k);
  }
}

##############################################################################
#
# ROUTINES FOR PRODUCING TeX OUTPUT
#
##############################################################################

##############################################################################
# TeX_preamble: returns preamble for TeX output, including definitions of 
#               TeX macros to be used in typesetting string modules
# 
#
sub TeX_preamble {
  return <<END_PREAMBLE
\\input amstex
\\newdimen\\vk
\\newdimen\\hk
\\newdimen\\vs
\\newdimen\\hs
\\vs=7pt
\\hs=5pt
\\vk=0pt
\\hk=0pt
\\catcode`<=\\active
\\catcode`>=\\active
\\catcode`^=\\active
\\catcode`~=\\active
\\catcode`:=\\active
\\catcode`!=\\active
\\catcode`;=\\active
\\def\\put#1{\\offinterlineskip\\vbox to0pt{\\kern-\\vk
     \\hbox to 0pt{\\kern\\hk\$\\scriptstyle #1\$\\hss}\\vss}}
\\def>#1{\\advance\\vk by -\\vs\\put #1}
\\def<#1{\\advance\\vk by \\vs\\put#1}
\\def^#1{\\advance\\hk by\\hs\\advance\\vk by\\vs\\put #1\\advance\\hk by \\hs}
\\def~#1{\\advance\\hk by\\hs\\advance\\vk by-\\vs\\put #1\\advance\\hk by \\hs}
\\def:#1{\\hskip3pt\\hk=0pt\\vk=0pt\\put#1}
\\def;{\\advance\\hk by 8pt\\hskip\\hk}
\\def!#1{\\hskip3pt\\hk=\\hs\\vk=0pt\\put #1\\hk=0pt}
\\def\\gldim{\\operatorname{gldim\\,}}
\\def\\ne{\\hidewidth\\mathop{\\nearrow}\\hidewidth}
\\def\\se{\\hidewidth\\mathop{\\searrow}\\hidewidth}
\\def\\dn{\\hidewidth\\mathop{\\downarrow}\\hidewidth}
\\def\\up{\\hidewidth\\mathop{\\uparrow}\\hidewidth}
\\def\\rr{\\hidewidth\\mathop{\\rightarrow}\\hidewidth}
\\nopagenumbers
END_PREAMBLE
}

##############################################################################
# vadjust: returns adjustment factor (float) for vertical alignment of
#          typeset string module
#
sub vadjust {
  local ($v)=@_[0];
  local ($min, $max, $j, $k, $c);
  $min=$max=$k=0;
  for ($j=0; $j<length($v); $j+=2) {
    $c=substr($v, $j, 1);
    if ($c=='<') {
      $min=$k if --$k < $min;
    }
    elsif ($c=='>') {
      $max=$k if ++$k > $max;
    }
  }
  return ($max + $min + 1) / 2;
}

##############################################################################
# baselineskip: returns baselineskip for the document...this is determined by
#               the maximal depth of a string module
#
sub baselineskip {
  local ($bskp, $vadj, $k);
  $bskp=0;
  foreach $k (keys %proj) {
    if (($vadj=&vadjust($proj{$k})) > $bskp) {
      $bskp=$vadj;
    }
  }
  $bskp*=2.5;
  return "\\baselineskip=".$bskp."\\vs";
}

##############################################################################
# TeXout_stringmod: produces TeX output for a string module
# 
#
sub TeXout_stringmod {
  local ($v)=@_[0];
  local ($texout);

  if (! $v) {
    return "";
  }
  elsif (&is_simple($v)) {
    return "S_".$v;
  }
# $texout="\\raise".&vadjust($v)."\\vs\\hbox{";
  $texout="\\vs\\hbox{";
	if ($is_projective{$v}) {
		$texout .= "\$\\bigl|\$";
	}
  if ($is_nupi{$v}) {
    $texout .= "!";
    # delete last two characters
    $v=substr($v, 0, -2);
  }
  else {
    $texout .= ":";
  }
  # replace <x> with ^x> and >x< with ~x<
  $v=~s/\<(.)>/^$1>/g;
  $v=~s/>(.)</~$1</g;
  $texout .= $v.";";
	if ($is_injective{$v} || $is_nupi{$v}) {
		$texout .= "\$\\bigr|\$";
	}
  $texout .= "}";
  return $texout;
}

##############################################################################
# TeXout_ARQ: produces TeX output for AR-quiver, which is passed to function
#             as hash of hashes described in get_cone
# 
sub TeXout_ARQ {
  local ($aref)=shift;
  local($i_min, $i_max, $j_min, $j_max);
  local ($arrows, $rarr);

  ($i_min, $i_max, $j_min, $j_max)=&cone_min_max($aref);
  print &TeX_preamble();
  print "\$\$\\matrix\n";
  for ($j=$j_max; $j>=$j_min; $j--) {
    for ($i=$i_max; $i>=$i_min; $i--) {
			$arrows .= "&";
			$rarr="";
      if ($$aref{$i}{$j}) {
        print &TeXout_stringmod($$aref{$i}{$j});
				if ($i>$i_min && $j>$j_min && $$aref{$i-1}{$j-1}) {
					$arrows .= "\\se";
				}
				if ($i>$i_min && $$aref{$i-1}{$j}) {
					$rarr="\\rarr";
				}
      }
      elsif ($i>$i_min && $j>$j_min && $$aref{$i}{$j-1} && $$aref{$i-1}{$j}) {
				$arrows .= "\\ne";
			}
      print "&$rarr&" if $i > $i_min;
			$arrows .= "&";
    }
    print "\\cr\n";
    print "$arrows\\cr\n" if $arrows;
    $arrows="";
  }
  print "\\endmatrix\$\$\n";
  print "\\vfil\\eject\\end\n";
}

##############################################################################
# MAIN:
#
# global variables:
#
# SPECIAL STRING MODULES
#
# %proj      : projective modules, indexed by simple module
#
# %inj       : injective modules which are not non-uniserial projective/injective,
#              indexed by simple module
#
# %radnupi   : radicals of non-uniserial projective/injective modules,
#              indexed by top of the corresponding proj/inj module
#
# %dradnupi  : dual of radical (P/soc P) of non-unis proj/inj modules,
#              indexed by top of the corresponding proj/inj module
#
# HASHES INDEXED BY STRING MODULE
#
# %is_projective : non-zero iff index is projective
# %is_injective: non-zero iff index is injective (but not NUPI)
# %is_nupi : non-zero iff index is NUPI
# %is_radnupi : non-zero iff index if radical of NUPI
# %is_dradnupi : non-zero iff index is dual of radical of NUPI (P/soc P)
#
#
# SPECIAL PATHS, INDEXED BY VERTEX OF INTEREST
#
# %lmaxdown  : maximal (non-zero) left path beginning at a vertex
# %rmaxdown  : maximal (non-zero) right path beginning at a vertex
# %lmaxup    : maximal (non-zero) left path ending at a vertex
# %rmaxup    : maximal (non-zero) right path beginning at a vertex
#
# FUNCTION RESULTS
# the results of L, R, DTr are kept in hashes to avoid recomputing
# %L         : results of L
# %R         : results of R
# %DTr       : results of DTr
#
# initialization...must be done roughly in this order
&init_projectives();
&init_maxdown();
&init_maxup();
&init_radnupi();
&init_injectives();

#&show_diagnostics();

foreach $k (keys %inj) {
  if (! $inj_min  || length($inj{$k}) < length($inj_min)) {
    $inj_min=$inj{$k};
  }
}

%c=&get_cone($inj_min);
($i_min, $i_max, $j_min, $j_max)=&cone_min_max(\%c);
for ($j=$j_max; $j>=$j_min; $j--) {
  print "%";
   for ($i=$i_max; $i>=$i_min; $i--) {
     if ($c{$i}{$j}) {
			 printf "[%2d,%2d]", $i, $j;
		 }
		 else {
			 printf " %2d,%2d ", $i, $j;
		 }
   }
   print "\n";
}

&TeXout_ARQ(\%c);

#&get_basis();
