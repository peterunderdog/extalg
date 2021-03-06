#!H:/usr/local/perl5/bin/perl
#
# $Header: X:/CVS/extalg/extalgperl/extalg.pl,v 1.5 2000/11/08 15:42:19 pb Exp $
#
# $Log: extalg.pl,v $
# Revision 1.5  2000/11/08 15:42:19  pb
# outputs projectives...other formatting changes
#
# Revision 1.4  2000/11/08 14:39:11  pb
# fixed syntax error..reads projectives from cgi
#
# Revision 1.3  2000/11/07 22:48:21  pb
# init projectives from cgi params
#
# Revision 1.2  2000/11/07 03:46:16  pb
# many changes, including better formatting of html, formatting of
# other output etc.
#
# Revision 1.1.1.1  2000/11/06 21:54:49  pbrown
# extalg import
#
# Revision 1.12  1999/05/11 06:43:22  pbrown
# finds minimal generators
#
# Revision 1.11  1999/05/06 05:58:07  pbrown
# get_basis works
#
# Revision 1.7  1999/04/26 05:51:11  pbrown
# fixed init_injectives..added tracking of injectives contained in cone
# produced by get_cone
#
# Revision 1.6  1999/04/25 18:26:41  pbrown
# fixed TeX output
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
  my $a=join('',reverse(split(//,shift)));
  $a=~tr/<>/></;
  return $a;
}

##############################################################################
# init_projectives: reads projective modules from array reference and constructs 
#                   associative array %proj where $proj{"s") is
#                   projective cover of simple module represented by "s"
#
# the projective modules and all other indecomposables in this program are
# string modules and are represented by 'primitive V-sequences' (see
# Skowronski-Waschbusch)...arrows are represented by '<' and '>', for example
# 5<4<3>2>1<6 represents the V-sequence
#
#      		 3
#      		/ \
#      	 4   2   6
#      	/     \ /
#      5       1
#
#
# a non-uniserial projective/injective module (henceforth abbreviated 
# "nupi") must be represented as follows:
#
# s<....<t>...>s where s=socle, and t=top, e.g., the nupi
#
#      		 3
#      		/ \
#      	 4   \
#        |    2 
#        5   / 
#         \ /
#          6
#
# would be represented as 6<5<4<3>2>6 (or 6<2<3<4>5>6)
#
sub init_projectives {
	my ($projs)=shift;
  my ($top, $rev);
	foreach my $p (@$projs)
  {
    $rev=0;
    if ($p =~ /^(.)$/)
      {$top=$1;}
    elsif ($p =~ /<(.)>/)
      {$top=$1;}
    elsif ($p =~ /^(.)>/)
      {$top=$1;}
    elsif ($p =~ /<(.)$/)
      {$top=$1; $rev=1;}
    else {die "Illegal format for ",$p ;}
    $proj{$top} = $rev ? &rev($p) : $p;
    $is_projective{$p}=$is_projective{&rev($p)}=$top;
  }
}

##############################################################################
# init_injectives: constructs associative array %inj where $inj{"s") is
# injective envelope of simple module represented by "s"...doesn't include
# NUPI's
# ..uses %lmaxup, %rmaxup, so call &getmaxup first
#
sub init_injectives {
  my (%i, $u);
  foreach $k (keys %lmaxup) {
    $i{$k}=$lmaxup{$k};
  }
  foreach $k (keys %rmaxup) {
    if ($i{$k}) {
      $i{$k}=&splice(&rev($rmaxup{$k}), $i{$k});
    }
    else {
      $i{$k}=$rmaxup{$k};
    }
  }
  foreach $k (keys %i) {
    unless ($is_radnupi{$i{$k}}) {
      $u=$inj{$k}=$i{$k};
      $is_injective{$u}=$is_injective{&rev($u)}=$k;
    }
  }
}

##############################################################################
# samestring: &samestring($a,$b) is TRUE if $a, $b represent same string
# module, i.e., either $a eq $b, or $a eq &rev($b) 
#
sub samestring {
  return ($_[0] eq $_[1]) || ($_[0] eq &rev($_[1]));
}

##############################################################################
# splice: splices two stringmods along common vertex...
# e.g., &splice("...y>x", "x<z...")=...y>x<z...returns "" if 
# 'common' vertices are different, or if y==z.
#
sub splice {
  my ($len0)=length($_[0]);
  my ($len1)=length($_[1]);
  my ($err)="Attempted to splice misformed strings $_[0] and $_[1]";
  my ($x);
  my ($y);
  if ($len0==0 || $len1==0) {
    return "";
  }
  elsif (substr($_[0],$len0-1,1) ne substr($_[1],0,1)) {
    return "";
  }
  elsif ($len0==1) {
    return $_[1];
  }
  elsif ($len1==1) {
    return $_[0];
  }
  elsif ($len0==2 || $len1==2) {
    die $err;
  }
  elsif (substr($_[0],$len0-3,1) eq substr($_[1],2,1)) {
    return "";
  }
  else {
    return $_[0].substr($_[1],1);
  }
}

##############################################################################
# init_nupi: creates associative arrays 
#            %radnupi: list of radicals of nonuniserial projective/injective modules
#            %dradnupi: list of duals of radicals of nonunis proj/inj (modulo socle)
#
sub init_nupi {
  my ($k, $u, @a);
  foreach $k (keys %proj) 
  {
    if ($proj{$k}=~/^((.)<.*)<.>(.*>(.))$/ && $2 eq $4)
    { 
      $u=$nupi{$k}=$proj{$k};
      $is_nupi{$u}=$k;
      $u=$radnupi{$k}=&splice($3,$1);
      $is_radnupi{$u}=$is_radnupi{&rev($u)}=$k;
      $u=$dradnupi{$k}=substr($proj{$k},2,-2);
      $is_dradnupi{$u}=$is_dradnupi{&rev($u)}=$k;
    }
  }
}

##############################################################################
# init_maxdown: creates associative arrays %lmaxdown, %rmaxdown, where  
# for each vertex 's', $lmaxdown('s') and %rmaxdown('s') are 
# paths starting at 's' maximal wrt property that they are not part
# of a relation defining the algebra; one of these paths may be 
# empty, and one may be trivial (i.e., only 's' itself)
#
# init_nupi MUST be called first 
#
sub init_maxdown {
  my ($k);
  my (@a);
  my ($p);
  my ($n);
  foreach $k (keys %proj) 
  {
    if ($is_nupi{$proj{$k}})
      {$p=$dradnupi{$k};}
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
  my ($k, @p, $p, $s);
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
  if ($_[0]=~/([^<>]>[^<]*$)/)
    {return $1;}
  elsif ($_[0]=~/(.)$/)
    {return $1;}
  else
    {return "";}
}

##############################################################################
# cut_tail: cuts the tail off...does nothing if no tail..returns empty if
#          stringmod is its own tail
#
sub cut_tail {
  my ($a)=shift;
  my ($tail)=&tail($a);
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
  my (@ext);
  my ($tail)=&tail($_[0]);
  my ($k)=substr($tail,0,1);
  my ($max);
  foreach $max ($lmaxdown{$k},$rmaxdown{$k})
  {
    if ($max=~/^$tail[>](.)/)
      {push(@ext,$_[0].">".$1) unless $_[0] =~/$1<.$/;}
  }
  return @ext;
}

##############################################################################
# RL: &R($a)=&RL(0,$a) 
#     &L($a)=&RL(1,$a)
#

sub RL {
  my ($L, $a)=@_;
  my (@ext);
  my ($x, $k, $sp, $max);

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
  my ($v)=shift;
  if (exists $R{$v}) {
    return $R{$v};
  }
  else {
    return $R{$v}=&RL(0, $v);
  }
}

##############################################################################
# L:
#
#
sub L {
  my ($v)=shift;
  if (exists $L{$v}) {
    return $L{$v};
  }
  else {
    return $L{$v}=&RL(1, $v);
  }
}

##############################################################################
# is_simple:
# returns true iff string module is simple
sub is_simple {
  length(shift)==1;
}

##############################################################################
# next_on_path:
# given v2->v1, next_on_path returns v3 such that v3->v2->v1 is sectional
# path, if it exists
#
sub next_on_path {
  my ($v2, $v1)=@_;
  my ($v2_L, $v2_R);
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
  my ($v)=shift;
  my ($u, $u_R, $v_LL, $v_RL);
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
  my ($v)=shift;

  if (exists $DTr{$v}) {
    return $DTr{$v};
  }
  else {
    return $DTr{$v}=&get_DTr($v);
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
  my ($v1, $v2)=@_;
  my ($v0)=&next_on_path($v1, $v2);
  return ($v0, $v1);
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
# parameters:
#   $v = the module whose cone we are computing
#   $iref = reference to hash keyed by injectives (non-NUPI) to
#           keep track of the injectives contained in the cone
#
# returns:
#   %c : the cone as a hash of hashes
#
sub get_cone {
  my ($v, $iref)=@_;
  my ($v2, $w2, $v1_, $w1_, $v2_, $w2_, $s);
  my (%DTrv);
  my ($i, $i_, $j_);
  my (%c);

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
		  if ($s=$is_dradnupi{$v1_}) {
			  $c{$i_+1}{$j}=$nupi{$s};
			}
			if ($i==0 && ($s=$is_injective{$v2_})) {
				$$iref{$s}=1;
			}
      $c{++$i_}{++$j_}=$v2_;
      ($v2_,$v1_)=&advance($v2_,$v1_);
    }
    $i_=$i;
    $j_=0;
    while ($w2_) {
			if ($s=$is_dradnupi{$w1_}) {
				$c{$i_+1}{$j}=$nupi{$s};
			}
			if ($i==0 && ($s=$is_injective{$w2_})) {
				$$iref{$s}=1;
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
# hh_min_max:
#
# given hash of hashes %c, returns
#
# i_min = min { i | $c{i}{j} is defined}
# i_max = max { i | $c{i}{j} is defined}
# j_min = min { j | $c{i}{j} is defined}
# j_max = max { j | $c{i}{j} is defined}
#
# as list (i_min, i_max, j_min, j_max)
#
sub hh_min_max {
  my ($cref)=shift;
  my (@i_)=sort {$a <=> $b} keys %$cref;
  my (@j_, %k, $i, $j);
  foreach $i (keys %$cref) {
    foreach $j (keys %{$$cref{$i}}) {
      $k{$j}=1;
    }
  }
   @j_=sort {$a <=> $b} keys %k;
  return ($i_[0], $i_[-1], $j_[0], $j_[-1]);
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
     \\hbox to 0pt{\\kern\\hk\$\\scriptstyle#1\$\\hss}\\vss}}
\\def>#1{\\advance\\vk by -\\vs\\put#1}
\\def<#1{\\advance\\vk by \\vs\\put#1}
\\def^#1{\\advance\\hk by0.5\\hs\\advance\\vk by\\vs\\put#1}
\\def~#1{\\advance\\hk by0.75\\hs\\advance\\vk by-\\vs\\put#1}
\\def:#1{\\hskip3pt\\hk=0pt\\vk=0pt\\put#1}
\\def;{\\advance\\hk by 8pt\\hskip\\hk}
\\def!#1{\\hskip3pt\\hk=\\hs\\vk=0pt\\put#1\\hk=0pt}
\\def\\gldim{\\operatorname{gldim\\,}}
\\def\\ne{\\hidewidth\\mathop{\\nearrow}\\hidewidth}
\\def\\se{\\hidewidth\\mathop{\\searrow}\\hidewidth}
\\def\\dn{\\hidewidth\\mathop{\\downarrow}\\hidewidth}
\\def\\up{\\hidewidth\\mathop{\\uparrow}\\hidewidth}
\\def\\rr{\\hidewidth\\mathop{\\rightarrow}\\hidewidth}
\\nopagenumbers
END_PREAMBLE
#`a useless comment to end the damn fontifying
}

##############################################################################
# vadjust: returns adjustment factor (float) for vertical alignment of
#          typeset string module
#
sub vadjust {
  my ($v)=shift;
  my ($min, $max, $j, $k, $c);
  $min=$max=$k=0;
  for ($j=1; $j<length($v); $j+=2) {
    $c=substr($v, $j, 1);
    if ($c=='<') {
      $min=$k if --$k < $min;
    }
    elsif ($c=='>') {
      $max=$k if ++$k > $max;
    }
  }
  return 0.5*($max + $min + 2);
}

##############################################################################
# baselineskip: returns baselineskip for the document...this is determined by
#               the maximal depth of a string module
#
sub baselineskip {
  my ($bskp, $vadj, $k);
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
  my ($v)=shift;
  my ($texout);

  if (! $v) {
    return "";
  }
  elsif (&is_simple($v)) {
    return "S_".$v;
  }
  $texout="\\raise".&vadjust($v)."\\vs\\hbox{";
#  $texout="\\hbox{";
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
  $v=~s/\<(.)\>/^$1~/g;
  $v=~s/[~>](.)[\^<]/~$1^/g;
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
  my ($aref)=shift;
  my ($i_min, $i_max, $j_min, $j_max);
  my ($arrows, $rarr);

  ($i_min, $i_max, $j_min, $j_max)=&hh_min_max($aref);
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
}

##############################################################################
#
# ROUTINES FOR PRODUCING PLAIN TEXT OUTPUT
#
##############################################################################
##############################################################################
# textout_ARQ: produces text output for AR-quiver, which is passed to function
#             as hash of hashes described in get_cone
# 
sub textout_ARQ {
  my ($aref)=shift;
  my ($i_min, $i_max, $j_min, $j_max);

  ($i_min, $i_max, $j_min, $j_max)=&hh_min_max($aref);
  for ($j=$j_max; $j>=$j_min; $j--) {
  	print "%";
  	 for ($i=$i_max; $i>=$i_min; $i--) {
  		 printf " %8s", $$aref{$i}{$j};
  	 }
  	 print "\n";
  }
}

##############################################################################
# text_out_stringmod
# 
sub text_stringmod {
  my $v=shift;
  my $s;
  
  $s .= "|" if ($is_projective{$v});
  $s .= $v;
  $s .= "|" if ($is_injective{$v} || $is_nupi{$v});
  return $s;
}

##############################################################################
# text_out_short_exact_sequence
# 
# prints short exact sequence
sub text_out_short_exact_sequence {
	my ($sref)=shift;
  my ($mid, @m, $x);

  for $x (@{$$sref{middle}}) {
		push @m, &text_stringmod($x) if $x;
	}
  $mid=join "(+)",@m;

  print " 0 $r_arrow ";
  print &text_stringmod($$sref{start});
  print " $r_arrow $mid $r_arrow ";
  print &text_stringmod($$sref{end});
  print "$r_arrow 0\n";
}

##############################################################################
#        ROUTINES FOR GENERATING HTML
##############################################################################
sub html_stringmod
{
	my $stringmod=shift;
	my $v=$stringmod;
	my $lev=0;
	my $col=0;
	my $maxcol=0;
	my @a=();
	my @res=();
	
  if ($is_nupi{$v}) {
		$v =~ s/>(.)$/!$1/;
		$v=substr $v, 2-length($v);
	}
  $v=~s/\<(.)\>/^$1~/g;
  $v=~s/[~>](.)[\^<]/~$1^/g;
	my @b=split //, $v;
  my %c = ( '<' => {col =>  0, lev => -1},
            '>' => {col =>  0, lev =>  1},
            '^' => {col =>  1, lev => -1},
            '~' => {col =>  1, lev =>  1},
            '!' => {col => -1, lev => 1});

	foreach $i (0..$#b) {
		if ($b[$i] =~ /[<>~\^!]/) {
			$lev += $c{$b[$i]}->{lev};
			$col += $c{$b[$i]}->{col};
			$maxcol=$col if $col > $maxcol;
			if ($lev < 0) {
				unshift @a, [];
				$lev=0;
			}
		}
		else {
			$a[$lev][$col]="<font size=-1><tt>" . $b[$i] . "</tt></font>";
		}
	}
	# indicate projective/injective modules
	if ($is_projective{$stringmod} || $is_nupi{$stringmod})
	{
		for my $ar (@a) {
			unshift @$ar, "|&nbsp;";
		}
	}
	if ($is_injective{$stringmod} || $is_nupi{$stringmod}) {
		for my $ar (@a) {
			$$ar[$maxcol+2]="&nbsp;|";
		}
	}
	push @res, "<!-- html_stringmod('$v') -->";
	push @res, "<table cellpadding=0 cellspacing=0>\n";
	for my $ar (@a) {
		push @res, "<tr>";
		push @res, map {&td($_)} @$ar;
		push @res, "</tr>";
	}
	push @res, "</table>";
	return join "\n", @res;
}

##############################################################################
# td:
# 
sub td {
	return "<td valign=center>" . $_[0] . "</td>";
}

##############################################################################
# td:
# 
sub tt {
	if ($textout) {
		return $_[0];
	}
	else {
   	return "<tt>" . $_[0] . "</tt>";
	}
}

##############################################################################
# html_out_short_exact_sequence
# 
# prints short exact sequence
sub html_out_short_exact_sequence {
	my ($sref)=shift;
  my ($mid, @m, $x);
	my $zero = &td(&tt("0"));
	my $arr=&td($r_arrow);
	my $oplus=&td(&tt("(+)"));

  for $x (@{$$sref{middle}}) {
		push @m, &td(&html_stringmod($x)) if $x;
	}
  $mid=join "$oplus",@m;

	print "<table cellpadding=0 cellspacing=0><tr>\n";
  print $zero, $arr;
  print &td(&html_stringmod($$sref{start}));
  print $arr , $mid , $arr;
  print &td(&html_stringmod($$sref{end}));
  print $arr, $zero;
	print "</tr></table>\n";
}

##############################################################################
#        OUTPUT ROUTINES - TEXT OR HTML
##############################################################################
sub newline {
	return $textout ? "\n" : "<BR>\n";
}

sub subscript {
	my $sub=shift;
	return $textout ? "_$sub" : "<sub>$sub</sub>";
}

sub out_short_exact_sequence {
	my $sref=shift;
	return $textout ? &text_out_short_exact_sequence($sref) : &html_out_short_exact_sequence($sref);
}

##############################################################################
#        ROUTINES FOR COMPUTING BASIS OF EXT-ALGEBRA
##############################################################################

##############################################################################
# short_exact_sequence
# constructor for short exact sequence, which is really an associative array
# with the following fields:
#
# if 0 --> A --> B --> C --> 0 is a short exact sequence represented by $s,
# then
#
#    $s{start}=A (as a string module)
#    $s{middle}=list of summands of middle term
#    $s{end}=C (as string module)
#
# usage:
#   %s=&short_exact_sequence($a, \@mid, $c)
#
sub short_exact_sequence {
	my ($a, $bref, $c)=@_;
	my (%s);
	$s{start}=$a;
	$s{middle}=[@$bref];
	$s{end}=$c;
	return %s;
}

##############################################################################
# returns 1 iff chain is periodic
#
sub is_periodic {
	my ($aref)=shift;
	my ($i);
  for $i (0 .. @$aref-1) { 
		return 1 if &samestring($$aref[-1]{start}, $$aref[$i]{end});
  }
	return 0;
}

##############################################################################
# get_basis
# 
# globals: 
#   @chain - current rectangular chain
#   @basis - list of chains forming basis
# 
sub get_basis {
  my ($max)=-1;
  my ($v)=@_;
	my ($v_0)=$v;
  my ($v_R)=&R($v);
  my ($v_L)=&L($v);
	my ($pathlen);
	my (@vpath, $s, $v_, $DTrv);
	my (@rc_nupi, @pathlen_nupi);
	my (@maxl);

  if ($is_projective{$v} || &is_periodic(\@chain)) {
		return;
	}
	push @vpath, $v_R;
  $pathlen=0;
  for (;;) {
	  $v_=$v;
		if ($v && !$is_projective{$v}) {
			for (;;) {
  			$DTrv=&DTr($v_);
				if ($s=$is_dradnupi{$v_}) {
          # we are maintaining parallel lists @rc_nupi, @pathlen_nupi, where
          # @rc_nupi = list of nupis in the rectangle we are currently looking at...
          # @pathlen_nupi = the value of $pathlen at the time the nupi was found...
					push @rc_nupi,$nupi{$s};
					push @pathlen_nupi,$pathlen;
				}
        if (&is_simple($DTrv)) {
			    push @basis, [@chain, {&short_exact_sequence($DTrv, [$vpath[$pathlen], $v_L, @rc_nupi], $v_0)}];
				}
  			last if (!$v_R || $is_projective{$v_R} || ($pathlen==$max && $max!=-1));
  			$pathlen++;
  			($v_R, $v_)=&advance($v_R, $v_);
  			push @vpath, $v_R if $max==-1;
			}
		}
		if (($max !=-1 && $pathlen<$max) || !$v || $is_projective{$v}) {
			push @chain, {&short_exact_sequence($v_ext, [$vpath[-1], $v, @rc_nupi], $v_0)};
      get_basis($DTrv);
			pop @chain;
		  pop @vpath;
      # the length of vpath decreased, so remove nupis which are
      # outside a rectangle of this length
			while (@pathlen_nupi && $pathlen_nupi[-1] > @vpath) {
				pop @rc_nupi;
				pop @pathlen_nupi;
			}
		}
		last if (!$v || $is_projective{$v});
		$max=$pathlen;
		$pathlen=0;
		$v_ext=$DTrv;
		$v_R=&DTr($v);
    ($v_L, $v)=&advance($v_L, $v);
	}
}

##############################################################################
# inside_rectangle:
# 
# returns TRUE iff stringmod $x is *inside* rectangle associated with 
# short exact sequence %s...here, *inside* means not on a sectional path 
# containing $s{end} 
#
sub inside_rectangle {
  my ($x, $s)=@_;
	my ($v)=$$s{end};
	my ($v_);
  my ($v_R)=&R($v);
  my ($v_L)=&L($v);
	my ($max)=-1;
	my ($pathlen)=0;
	my ($max_R)=$$s{middle}[0];
	my ($max_L)=$$s{middle}[1];

  for (;;) {
		$v_=$v;
		$pathlen=0;
		for (;;) {
			return 1 if &samestring(&DTr($v_), $x);
      ($v_R, $v_)=&advance($v_R, $v_);
			++$pathlen;
			last if (($max==-1 && &samestring($v_, $max_R)) || $pathlen==$max);
		}
		$max=$pathlen;
		$pathlen=0;
		$v_R=&DTr($v);
    ($v_L, $v)=&advance($v_L, $v);
		last if (&samestring($v, $max_L));
	}
	return 0;
}

##############################################################################
# congruent_chains:
# 
# returns TRUE iff exact sequence represented by rectangular chain c is
# congruent to basis element $basis[$ind]
#
sub congruent_chains {
  my ($c, $ind)=@_;
	my ($i);

  if ($#$c != $#{$basis[$ind]}) {
		return 0;
	}
	elsif (!&samestring($$c[0]{end}, $basis[$ind][0]{end})) {
		return 0;
	}
	elsif (!&samestring($$c[-1]{start}, $basis[$ind][-1]{start})) {
		return 0;
	}
	else {
		for $i (0..$#$c) {
			if (!&inside_rectangle($$c[$i]{start}, \%{$basis[$ind][$i]})) {
				return 0;
			}
		}
	}
	return 1;
}

##############################################################################
# find_equiv_chains:
#
# attempts to find chains of basis elements equivalent to $basis[$index]
# ..creates lists $eq_chains{$i} containing chains equivalent to @basis[$i],
# where chains are given as list of indices of basis elements in the chain
# 
sub find_equiv_chains {
	my ($index)=shift;
  my ($start, $deg, $ch);
  my ($i, $j, @chain);

  if (@_) {
    ($start, $deg, $ch)=@_;
	}
	else {
		$start=$basis[$index][-1]{start};
		$deg=$#{$basis[$index]}+1;
		@$ch=();
	}

  foreach $i (0 .. $#basis) {
		if ($i != $index && $#{$eq_chains[$i]}==-1 && &samestring($basis[$i][0]{start}, $start)) {
			if ($#{$basis[$i]}+1==$deg && &samestring($basis[$i][0]{end},$basis[$index][0]{end})) {
				push @$ch, $i;
				foreach $j (@$ch) {
					unshift @chain, @{$basis[$j]};
				}
				if (&congruent_chains(\@chain, $index)) {
					print "b<sub>$index</sub> =~ ";
          foreach $j (@$ch) {
						print "b<sub>$j</sub>";
          } 
          print "<br>\n";
          push @{$eq_chains[$index]},[@$ch];
				}
				@chain=();
        pop @$ch;
			}
		  elsif ($#{$basis[$i]}+1 < $deg) {
		  	push @$ch,$i;
        &find_equiv_chains($index, $basis[$i][0]{end}, $deg-$#{$basis[$i]}-1, $ch);
				pop @$ch;
			}
		}
	}
}
 
##############################################################################
# zero_relations:
# 
sub zero_relations {
	my ($v)=shift;
  my ($ch, $i, $j, $chain, $n, $is_zero_relation);

	if (@_) {
		$ch=@_;
	}
	else {
		@$ch=();
	}

	foreach $i (0 .. $#basis) {
    # the eq_chains condition is for checking that the basis element is minimal
		if ($#{$eq_chains[$i]}==-1 && &samestring($basis[$i][0]{start}, $v)) {
			push @$ch, $i;
			if (@$ch==1) {
				$is_zero_relation=0;
			}
			else {
				$is_zero_relation=1;
				$chain=join " ", @$ch;
				for $n (@{$non_zero_chains{$v}}) {
					if ($chain eq join " ", @$n) {
						$is_zero_relation=0;
						last;
					}
				}
			}
      if ($is_zero_relation) {
        push @zero_relations, [@$ch];
			}
			else {
				&zero_relations($basis[$i][-1]{end}, $ch);
			}
			pop @$ch;
		}
	}
}

##############################################################################
# get_zero_relations:
# 
# gets zero relations
# 
sub get_zero_relations {
  my ($a, $b, $k);

  # first create array non_zero_chains for use by
  # zero_relations
  foreach $a (@eq_chains) {
		foreach $b (@{$a}) {
      push @{$non_zero_chains{@$b[0]}}, $b;
		}
	}
	foreach $k (keys %proj) {
		&zero_relations($k);
	}
}

##############################################################################
# get_minl_generators:
# 
sub get_minl_generators {
	my ($i)=0;

  for $i (0..$#basis) {
		&find_equiv_chains($i);
	}
}

##############################################################################
# html_out_projectives:
#
sub html_out_projectives {
	print $q->start_table;
	foreach my $s (sort keys %proj) {
		print "<tr>";
		print &td(&tt("P<sub>$s</sub>"));
		print &td(&tt("="));
		print &td(html_stringmod($proj{$s}));
		print "</tr>\n";
		print "<tr><td>&nbsp;</td></tr>\n"; # blank line
	}
	print $q->end_table;
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
# the following are indexed by representations of string modules,
# and are defined iff "is_xxxx" holds for the string module index...
# furthermore, the value represents the index of  
#
# %is_projective : index is projective, returns top of projective
# %is_injective: index is injective (but not NUPI), returns its socle
# %is_nupi : index is NUPI, returns top
# %is_radnupi : index is radical of NUPI, returns top
# %is_dradnupi : index is dual of radical of NUPI (P/soc P), returns top
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
# BASIS
# @chain     : used by get_basis to keep track of current max'l chain
# @basis     : list of basis elements of Ext-algebra..
#
#
# initialization...must be done roughly in this order

use CGI;
$q=new CGI;

@projectives=$q->param('p');
$textout=$q->param('textout');

$title=$ARGV[0];
&init_projectives(\@projectives);
&init_nupi();
&init_maxdown();
&init_maxup();
&init_injectives();
$r_arrow=$textout ? "-->" : &tt("&nbsp;--&gt;&nbsp;");

if ($textout)
{
  &show_diagnostics();
  
  @ikeys = sort {length($inj{$a}) <=> length($inj{$b})} keys %inj;
  foreach $ik (@ikeys) {
  	print "$ik ";
  	print $in_cone{$ik} ? "in a cone\n\n" : "not in a cone\n\n";
  	if (!$in_cone{$ik}) {
  	print "\n\n\$C($ik)\$\n\n";
  		print "%get_cone(inj\{$ik\}):\n";
  		%c=&get_cone($inj{$ik}, \%in_cone);
  		&textout_ARQ(\%c);
  	}
  }
}

foreach $k (keys %proj) {
	&get_basis($k);
}
@basis = sort {$#$b <=> $#$a} @basis;

unless ($textout)
{
  print $q->header;
  print $q->start_html("Ext-algebras of special biserial algebras of finite representation type",
  										 "pbrown\@cs.umass.edu");
  print $q->h1('Indecomposable projective modules:');
  &html_out_projectives();

  print $q->h1('Basis for the Ext-algebra:');
}

$i=0;
foreach $v (@basis) {
  print &tt("b" . &subscript($i) . ":"), "\n";
	$i++;
  foreach $b (@{$v}) {
    &out_short_exact_sequence(\%{$b});
		print &newline();
	}
  print &newline, &newline;
}

unless ($textout)
{
  print $q->h1("Equivalent chains:");
  print "<tt>\n";
  &get_minl_generators();
  print "</tt>\n";
  &get_zero_relations();
  
  print $q->h1("Minimal generators:");
  print "<tt>\n";
  foreach $i (0..$#basis) {
  	if ($#{$eq_chains[$i]}==-1) {
  		$deg=$#{$basis[$i]}+1;
  		print "b<sub>$i</sub>: $basis[$i][0]{end} --&gt; $basis[$i][-1]{start}, deg=$deg<br>\n";
  	}
  }
  print "</tt>\n";
  
  print $q->h1("Zero relations");
  print "<tt>\n";
  foreach $z (@zero_relations) {
  	foreach $n (@{$z}) {
  		print "b<sub>$n</sub>";
  	}
  	print "= 0<br>\n";
  }
  print "</tt>\n";
  
  print $q->end_html;
}
